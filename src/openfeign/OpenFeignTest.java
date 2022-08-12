package src.openfeign;

import feign.*;
import feign.codec.Decoder;
import feign.gson.GsonDecoder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author caoyang
 * @create 2022-08-12 16:35
 */
@Slf4j
public class OpenFeignTest {

    interface GitHub{
        @Data
        class Contributor{
            String login;
            int contributions;
        }
        @RequestLine("GET /repos/{owner}/{repo}/contributors")
        List<Contributor> contributors(@Param("owner") String owner, @Param("repo") String repo);

        static GitHub connect() {
            Decoder decoder = new GsonDecoder();
            return Feign.builder()
                    .decoder(decoder)
                    .logger(new Logger.ErrorLogger())
                    .logLevel(Logger.Level.BASIC)
                    .target(GitHub.class, "https://api.github.com");
        }
    }

    interface Avatar{
        @RequestLine("GET /api/v1/avatar/workflow/flow_template?template_name={name}")
        @Headers({"Cookie: {cookie}"})
        Object flowTemplate(@Param("name") String name, String cookie);

        static Avatar connect(){
            Decoder decoder = new GsonDecoder();
            return Feign.builder()
                    .decoder(decoder)
                    .logger(new Logger.ErrorLogger())
                    .logLevel(Logger.Level.BASIC)
                    .target(Avatar.class, "http://mbop.oversea.test.sankuai.com");
        }
    }

    public static void main(String[] args) {
        GitHub github = GitHub.connect();
        List<GitHub.Contributor> contributors = github.contributors("netflix", "feign");
        for (GitHub.Contributor contributor : contributors) {
            log.info(contributor.toString());
        }

        Avatar avatar = Avatar.connect();
        String cookie = "JSESSIONID=node03e9k4o69wqqdutyau4wz2m3l244.node0; " +
                "_lxsdk_test=1; " +
                "currentURL=http://mbop.oversea.test.sankuai.com/#/home; " +
                "logan_session_token=wowpdw96qnglonvujreo; mbop-ssoid=eAGFjitLBFEYQLmsyLJJJhkn7k6Q7965r88kszOL0UcQLHJfE_UPGFyLCCbbqsGFBRVRsQmaLJbFJAwsGNX5B1YVMZvP4XCaZGY42GvEk-fH6gJYy9EAKKzgcj6WGFSQsuQ2IE-lQGd5YlCVACoYCNkLiTprwa66sBm2CyxYjqmgjOaC9zKETGE3yzVHVmQ9Gb9W98dX0Cbs37D-WVpoLI6fqsk5LH0ejB9OYJckrenlle6WD1H0Njqrb4fv-5cfp_36elTf9Gen4p27Qaf9Kx-S5t_YEZlLS-ZLZYIM3FDvEJjWXgnLLOegrd6gUkikIJVSAtdj6cB_U27KJOVeKesZNUI7is4pr80XfShmfg**eAEFwQkBwDAIA0BL0PLKSQPzL2F3iDF8bj4iJnKDWg_k1syhvkbvJosdyEmk8wR29VZqjv1VCRJC**eQy3fOH4Q0mtTqsCtyPz2wsFvYBLplK-ulLbcwPb7mugxeQ1Mb351wZnBFLGWSmslHa25VN5e1mYfeGYx4bcxA**NjQwOTM3NyxjYW95YW5nNDIs5pu56ZizLGNhb3lhbmc0MkBtZWl0dWFuLmNvbSwxLDAzMjU3ODgxLDE2NjA0NTY4MzkwMDI; " +
                "session=eyJDQVNfVVNFUk5BTUUiOiJjYW95YW5nNDIifQ.FZ2mmQ.w-gGXqaWcu1CcBNi_D_t7XAGqrc; " +
                "sso_id=eAGFjitLBFEYQLmsyLJJJhkn7k6Q7965r88kszOL0UcQLHJfE_UPGFyLCCbbqsGFBRVRsQmaLJbFJAwsGNX5B1YVMZvP4XCaZGY42GvEk-fH6gJYy9EAKKzgcj6WGFSQsuQ2IE-lQGd5YlCVACoYCNkLiTprwa66sBm2CyxYjqmgjOaC9zKETGE3yzVHVmQ9Gb9W98dX0Cbs37D-WVpoLI6fqsk5LH0ejB9OYJckrenlle6WD1H0Njqrb4fv-5cfp_36elTf9Gen4p27Qaf9Kx-S5t_YEZlLS-ZLZYIM3FDvEJjWXgnLLOegrd6gUkikIJVSAtdj6cB_U27KJOVeKesZNUI7is4pr80XfShmfg**eAEFwQkBwDAIA0BL0PLKSQPzL2F3iDF8bj4iJnKDWg_k1syhvkbvJosdyEmk8wR29VZqjv1VCRJC**eQy3fOH4Q0mtTqsCtyPz2wsFvYBLplK-ulLbcwPb7mugxeQ1Mb351wZnBFLGWSmslHa25VN5e1mYfeGYx4bcxA**NjQwOTM3NyxjYW95YW5nNDIs5pu56ZizLGNhb3lhbmc0MkBtZWl0dWFuLmNvbSwxLDAzMjU3ODgxLDE2NjA0NTY4MzkwMDI; " +
                "ssoid=eAGFjitLBFEYQLmsyLJJJhkn7k6Q7965r88kszOL0UcQLHJfE_UPGFyLCCbbqsGFBRVRsQmaLJbFJAwsGNX5B1YVMZvP4XCaZGY42GvEk-fH6gJYy9EAKKzgcj6WGFSQsuQ2IE-lQGd5YlCVACoYCNkLiTprwa66sBm2CyxYjqmgjOaC9zKETGE3yzVHVmQ9Gb9W98dX0Cbs37D-WVpoLI6fqsk5LH0ejB9OYJckrenlle6WD1H0Njqrb4fv-5cfp_36elTf9Gen4p27Qaf9Kx-S5t_YEZlLS-ZLZYIM3FDvEJjWXgnLLOegrd6gUkikIJVSAtdj6cB_U27KJOVeKesZNUI7is4pr80XfShmfg**eAEFwQkBwDAIA0BL0PLKSQPzL2F3iDF8bj4iJnKDWg_k1syhvkbvJosdyEmk8wR29VZqjv1VCRJC**eQy3fOH4Q0mtTqsCtyPz2wsFvYBLplK-ulLbcwPb7mugxeQ1Mb351wZnBFLGWSmslHa25VN5e1mYfeGYx4bcxA**NjQwOTM3NyxjYW95YW5nNDIs5pu56ZizLGNhb3lhbmc0MkBtZWl0dWFuLmNvbSwxLDAzMjU3ODgxLDE2NjA0NTY4MzkwMDI";
        Object obj = avatar.flowTemplate("service_whitelist",cookie);
        log.info("flow: {}", obj);

    }
}
