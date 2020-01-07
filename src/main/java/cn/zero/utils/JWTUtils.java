package cn.zero.utils;

import cn.zero.spider.pojo.ResponseData;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class JWTUtils {

    /**
     * 私钥密码，保存在服务器，客户端是不会知道密码的，以防止被攻击
     */
    private static final String SECRET = "ldpwsnbb";

    /**
     * 加密方式
     */
    private static final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    /**
     *  过期时间（单位分），这里暂定3天
     */
    private static final int TIME = 3 * 24 * 60;

    /**
     * 对密钥进行加密
     * @return
     */
    private static Key getKey(){
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(SECRET);
        return  new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
    }

    /**
     * 生成Token
     * JWT分成3部分：1.头部（header),2.载荷（payload, 类似于飞机上承载的物品)，3.签证（signature)
     * 加密后这3部分密文的字符位数为：
     *  1.头部（header)：36位，Base64编码
     *  2.载荷（payload)：没准，BASE64编码
     *  3.签证（signature)：
     *      43位，将header和payload拼接生成一个字符串，
     *      使用HS256算法和我们提供的密钥（secret,服务器自己提供的一个字符串），对str进行加密生成最终的JWT
     * @return 生成的token
     */
    public static String createToken(String username) {

        // 签发时间
        Date iatDate = new Date();

        // 得到过期时间(3天)
        LocalDateTime exp = LocalDateTime.now().plusMinutes(TIME);
        Date dd = Date.from(exp.toInstant(ZoneOffset.UTC));

        // 组合header
        Map<String, Object> map = new HashMap<>();
        map.put("alg", "HS256");
        map.put("typ", "JWT");
        return Jwts.builder()
                .setHeaderParams(map)
                .claim("username",username)
                .setExpiration(dd)
                .setIssuedAt(iatDate)
                .signWith(SignatureAlgorithm.HS256, getKey()).compact();
    }

    /**
     * 解密Token查看其是否合法
     * @param token 需要进行校验的Token
     * @return 负载内容Body
     */
    public static /*ResponseData<Claims>*/ Claims verifyToken(String token) {
//        if (StringUtils.isBlank(token)) {
//            return ResponseData.<Claims>builder().code(500).message("Token不能为空").build();
//        }
        try {
            Claims claims = Jwts.parser().setSigningKey(getKey()).parseClaimsJws(token).getBody();
            //return ResponseData.<Claims>builder().status(true).data(claims).build();
            return claims;
        } catch (ExpiredJwtException e){
            log.warn("Token: {} ,超时", token);
           // return ResponseData.<Claims>builder().code(1001).build();
        } catch (Exception e){
            log.warn("解析Token: {} ,Exception：{}", token, e.getMessage());
            //return ResponseData.<Claims>builder().code(500).message(e.getMessage()).build();
        }
        return null;
    }
}
