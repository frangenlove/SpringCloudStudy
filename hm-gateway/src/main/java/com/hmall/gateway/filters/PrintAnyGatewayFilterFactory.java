package com.hmall.gateway.filters;

import lombok.Data;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.List;

@Component//PrintAnyGatewayFilterFactory
public class PrintAnyGatewayFilterFactory extends AbstractGatewayFilterFactory<PrintAnyGatewayFilterFactory.Config> {
    @Override
    public GatewayFilter apply(Config config) {
        /**
         * OrderedGatewayFilter
         * - GatewayFilter: 过滤器
         * - int: 优先级，值越小优先级越高
         */
        return new OrderedGatewayFilter(new GatewayFilter(){
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                // 获取config值
                String a = config.getA();
                String b = config.getB();
                String c = config.getC();
                // 编写过滤器逻辑
                System.out.println("a = " + a);
                System.out.println("b = " + b);
                System.out.println("c = " + c);
                // 放行
                return chain.filter(exchange);
            }
        },1);
    }

    //自定义配置类，注意成员变量名称
    @Data
    static class Config{
        private String a;
        private String b;
        private String c;
    }

    //将变量名称依次返回，顺序很重要
    @Override
    public List<String> shortcutFieldOrder() {
        return List.of("a","b","c");
    }
    //将config字节码传递给父类，父类帮助读取yaml配置
    @Override
    public Class<Config> getConfigClass() {
        return Config.class;
    }
}
