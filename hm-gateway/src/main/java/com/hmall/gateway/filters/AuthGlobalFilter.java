package com.hmall.gateway.filters;

import cn.hutool.core.text.AntPathMatcher;
import com.hmall.common.exception.UnauthorizedException;
import com.hmall.gateway.config.AuthProperties;
import com.hmall.gateway.utils.JwtTool;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {
    private final AuthProperties authProperties;
    private final JwtTool jwtTool;
    /**
     * AntPathMatcher是一个工具类，通常不需要Spring管理其生命周期。得自己new一个
     * 工具类是无状态的，可以在任何地方实例化和使用。
     * 非单例：由于AntPathMatcher是无状态的，它不需要作为单例Bean管理。每次使用时创建一个新的实例是安全的，不会导致资源浪费或状态不一致的问题。
     * 简单性：手动创建AntPathMatcher实例非常简单，只需要使用new关键字即可。这种方式代码清晰，易于理解。
     * 专门用于匹配yaml文件中- /search/**的路径
     */
    private final AntPathMatcher matcher = new AntPathMatcher();
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        //1. 获取request
        ServerHttpRequest request = exchange.getRequest();
        //2. 判断是否需要拦截
        if(isExclude(request.getPath().toString())){
            return chain.filter(exchange);
        }
        //3 .获取token
        String token = null;
        List<String> headers = request.getHeaders().get("authorization");
        if(headers!=null && !headers.isEmpty()){
            token=headers.get(0);
        }
        //4. 校验并解析jwt
        Long userId=null;
        try{
            userId = jwtTool.parseToken(token);
        }catch (UnauthorizedException e){
            //设置响应状态码为401
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        //5. 传递用户信息
        String userInfo = userId.toString();
        ServerWebExchange swe = exchange.mutate()//mutate方法就是对下游请求做更改
                .request(builder -> builder.header("user-info", userInfo))
                .build();//构建完后生成一个新的exchange
        //6. 放行
        return chain.filter(swe);
    }

    private boolean isExclude(String path) {
        for (String pathPattern : authProperties.getExcludePaths()) {
            if(matcher.match(pathPattern,path))
                return true;
        }
        return  false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
