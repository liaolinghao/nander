package wang.bigbird.domain.framework.server.web.auth.domain.pojo.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

/**
 * 用户功能点
 *
 * @author Bigbird
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtFunction implements GrantedAuthority {

    private Long id;

    private String name;

    /**
     * 功能点表达式，
     * 为具体的某一项功能点，比如：GIS
     */
    private String pattern;

    @JsonIgnore
    @Override
    public String getAuthority() {
        return pattern;
    }

}
