package java.lang.invoke;

import java.util.ArrayList;
import java.util.Arrays;
import static java.lang.invoke.LambdaForm.*;
import static java.lang.invoke.LambdaForm.BasicType.*;

final class LambdaFormBuffer {
    private int arity, length;
    private Name[] names;
    private Name[] originalNames;  // snapshot of pre-transaction names
    private byte flags;
    private int firstChange;
    private Name resultName;
    private String debugName;
    private ArrayList<Name> dups;

    private static final int F_TRANS = 0x10, F_OWNED = 0x03;

    LambdaFormBuffer(LambdaForm lf) {
        this.arity = lf.arity;
        setNames(lf.names);
        int result = lf.result;
        if (result == LAST_RESULT)  result = length - 1;
        if (result >= 0 && lf.names[result].type != V_TYPE)
            resultName = lf.names[result];
        debugName = lf.debugName;
        assert(lf.nameRefsAreLegal());
    }

    private  lambdaForm() {
        assert(!inTrans());  // need endEdit call to tidy things up
        return new LambdaForm(debugName, arity, nameArray(), resultIndex());
    }

    Name name(int i) {
        assert(i < length);
        return names[i];
    }

    Name[] nameArray() {
        return Arrays.copyOf(names, length);
    }

    int resultIndex() {
        if (resultName == null)  return VOID_RESULT;
        int index = indexOf(resultName, names);
        assert(index >= 0);
        return index;
    }

    void setNames(Name[] names2) {
        names = originalNames = names2;  // keep a record of where everything was to start with
        length = names2.length;
        flags = 0;
    }

    private boolean verifyArity() {
        for (int i = 0; i < arity && i < firstChange; i++) {
            assert(names[i].isParam()) : "#" + i + "=" + names[i];
        }
        for (int i = arity; i < length; i++) {
            assert(!names[i].isParam()) : "#" + i + "=" + names[i];
        }
        for (int i = length; i < names.length; i++) {
            assert(names[i] == null) : "#" + i + "=" + names[i];
        }
        // check resultName also
        if (resultName != null) {
            int resultIndex = indexOf(resultName, names);
            assert(resultIndex >= 0) : "not found: " + resultName.exprString() + Arrays.asList(names);
            assert(names[resultIndex] == resultName);
        }
        return true;
    }

    private boolean verifyFirstChange() {
        assert(inTrans());
        for (int i = 0; i < length; i++) {
            if (names[i] != originalNames[i]) {
                assert(firstChange == i) : Arrays.asList(firstChange, i, originalNames[i].exprString(), Arrays.asList(names));
                return true;
            }
        }
        assert(firstChange == length) : Arrays.asList(firstChange, Arrays.asList(names));
        return true;
    }

    private static int indexOf(NamedFunction fn, NamedFunction[] fns) {
        for (int i = 0; i < fns.length; i++) {
            if (fns[i] == fn)  return i;
        }
        return -1;
    }

    private static int indexOf(Name n, Name[] ns) {
        for (int i = 0; i < ns.length; i++) {
            if (ns[i] == n)  return i;
        }
        return -1;
    }

    boolean inTrans() {
        return (flags & F_TRANS) != 0;
    }

    int ownedCount() {
        return flags & F_OWNED;
    }

    void growNames(int insertPos, int growLength) {
        int oldLength = length;
        int newLength = oldLength + growLength;
        int oc = ownedCount();
        if (oc == 0 || newLength > names.length) {
            names = Arrays.copyOf(names, (names.length + growLength) * 5 / 4);
            if (oc == 0) {
                flags++;
                oc++;
                assert(ownedCount() == oc);
            }
        }
        if (originalNames != null && originalNames.length < names.length) {
            originalNames = Arrays.copyOf(originalNames, names.length);
            if (oc == 1) {
                flags++;
                oc++;
                assert(ownedCount() == oc);
            }
        }
        if (growLength == 0)  return;
        int insertEnd = insertPos + growLength;
        int tailLength = oldLength - insertPos;
        System.arraycopy(names, insertPos, names, insertEnd, tailLength);
        Arrays.fill(names, insertPos, insertEnd, null);
        if (originalNames != null) {
            System.arraycopy(originalNames, insertPos, originalNames, insertEnd, tailLength);
            Arrays.fill(originalNames, insertPos, insertEnd, null);
        }
        length = newLength;
        if (firstChange >= insertPos) {
            firstChange += growLength;
        }
    }

    int lastIndexOf(Name n) {
        int result = -1;
        for (int i = 0; i < length; i++) {
            if (names[i] == n)  result = i;
        }
        return result;
    }

    private void noteDuplicate(int pos1, int pos2) {
        Name n = names[pos1];
        assert(n == names[pos2]);
        assert(originalNames[pos1] != null);  // something was replaced at pos1
        assert(originalNames[pos2] == null || originalNames[pos2] == n);
        if (dups == null) {
            dups = new ArrayList<>();
        }
        dups.add(n);
    }

    private void clearDuplicatesAndNulls() {
        if (dups != null) {
            // Remove duplicates.
            assert(ownedCount() >= 1);
            for (Name dup : dups) {
                for (int i = firstChange; i < length; i++) {
                    if (names[i] == dup && originalNames[i] != dup) {
                        names[i] = null;
                        assert(Arrays.asList(names).contains(dup));
                        break;  // kill only one dup
                    }
                }
            }
            dups.clear();
        }
        // Now that we are done with originalNames, remove "killed" names.
        int oldLength = length;
        for (int i = firstChange; i < length; i++) {
            if (names[i] == null) {
                System.arraycopy(names, i + 1, names, i, (--length - i));
                --i;  // restart loop at this position
            }
        }
        if (length < oldLength) {
            Arrays.fill(names, length, oldLength, null);
        }
        assert(!Arrays.asList(names).subList(0, length).contains(null));
    }

    void startEdit() {
        assert(verifyArity());
        int oc = ownedCount();
        assert(!inTrans());  // no nested transactions
        flags |= F_TRANS;
        Name[] oldNames = names;
        Name[] ownBuffer = (oc == 2 ? originalNames : null);
        assert(ownBuffer != oldNames);
        if (ownBuffer != null && ownBuffer.length >= length) {
            names = copyNamesInto(ownBuffer);
        } else {
            // make a new buffer to hold the names
            final int SLOP = 2;
            names = Arrays.copyOf(oldNames, Math.max(length + SLOP, oldNames.length));
            if (oc < 2)  ++flags;
            assert(ownedCount() == oc + 1);
        }
        originalNames = oldNames;
        assert(originalNames != names);
        firstChange = length;
        assert(inTrans());
    }

    private void changeName(int i, Name name) {
        assert(inTrans());
        assert(i < length);
        Name oldName = names[i];
        assert(oldName == originalNames[i]);  // no multiple changes
        assert(verifyFirstChange());
        if (ownedCount() == 0)
            growNames(0, 0);
        names[i] = name;
        if (firstChange > i) {
            firstChange = i;
        }
        if (resultName != null && resultName == oldName) {
            resultName = name;
        }
    }

    void setResult(Name name) {
        assert(name == null || lastIndexOf(name) >= 0);
        resultName = name;
    }

    LambdaForm endEdit() {
        assert(verifyFirstChange());
        // Assuming names have been changed pairwise from originalNames[i] to names[i],
        // update arguments to ensure referential integrity.
        for (int i = Math.max(firstChange, arity); i < length; i++) {
            Name name = names[i];
            if (name == null)  continue;  // space for removed duplicate
            Name newName = name.replaceNames(originalNames, names, firstChange, i);
            if (newName != name) {
                names[i] = newName;
                if (resultName == name) {
                    resultName = newName;
                }
            }
        }
        assert(inTrans());
        flags &= ~F_TRANS;
        clearDuplicatesAndNulls();
        originalNames = null;
        // If any parameters have been changed, then reorder them as needed.
        // This is a "sheep-and-goats" stable sort, pushing all non-parameters
        // to the right of all parameters.
        if (firstChange < arity) {
            Name[] exprs = new Name[arity - firstChange];
            int argp = firstChange, exprp = 0;
            for (int i = firstChange; i < arity; i++) {
                Name name = names[i];
                if (name.isParam()) {
                    names[argp++] = name;
                } else {
                    exprs[exprp++] = name;
                }
            }
            assert(exprp == (arity - argp));
            // copy the exprs just after the last remaining param
            System.arraycopy(exprs, 0, names, argp, exprp);
            // adjust arity
            arity -= exprp;
        }
        assert(verifyArity());
        return lambdaForm();
    }

    private Name[] copyNamesInto(Name[] buffer) {
        System.arraycopy(names, 0, buffer, 0, length);
        Arrays.fill(buffer, length, buffer.length, null);
        return buffer;
    }

    LambdaFormBuffer replaceFunctions(NamedFunction[] oldFns, NamedFunction[] newFns,
                                      Object... forArguments) {
        assert(inTrans());
        if (oldFns.length == 0)  return this;
        for (int i = arity; i < length; i++) {
            Name n = names[i];
            int nfi = indexOf(n.function, oldFns);
            if (nfi >= 0 && Arrays.equals(n.arguments, forArguments)) {
                changeName(i, new Name(newFns[nfi], n.arguments));
            }
        }
        return this;
    }

    private void replaceName(int pos, Name binding) {
        assert(inTrans());
        assert(verifyArity());
        assert(pos < arity);
        Name param = names[pos];
        assert(param.isParam());
        assert(param.type == binding.type);
        changeName(pos, binding);
    }

    LambdaFormBuffer renameParameter(int pos, Name newParam) {
        assert(newParam.isParam());
        replaceName(pos, newParam);
        return this;
    }

    LambdaFormBuffer replaceParameterByNewExpression(int pos, Name binding) {
        assert(!binding.isParam());
        assert(lastIndexOf(binding) < 0);  // else use replaceParameterByCopy
        replaceName(pos, binding);
        return this;
    }

    LambdaFormBuffer replaceParameterByCopy(int pos, int valuePos) {
        assert(pos != valuePos);
        replaceName(pos, names[valuePos]);
        noteDuplicate(pos, valuePos);  // temporarily, will occur twice in the names array
        return this;
    }

    private void insertName(int pos, Name expr, boolean isParameter) {
        assert(inTrans());
        assert(verifyArity());
        assert(isParameter ? pos <= arity : pos >= arity);
        growNames(pos, 1);
        if (isParameter)  arity += 1;
        changeName(pos, expr);
    }

    LambdaFormBuffer insertExpression(int pos, Name expr) {
        assert(!expr.isParam());
        insertName(pos, expr, false);
        return this;
    }

    LambdaFormBuffer insertParameter(int pos, Name param) {
        assert(param.isParam());
        insertName(pos, param, true);
        return this;
    }
}
