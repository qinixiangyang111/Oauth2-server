package com.yueyang.oauth.config;

import org.apache.http.auth.AuthenticationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import javax.sql.DataSource;

/**
 * @program: Oauth2-server
 * @description:
 * @author: qinxiangyang
 * @create: 2020-06-08 22:11
 **/
@Configuration
@EnableAuthorizationServer // 开启认证授权中心
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {
    	// @EnableAuthorizationServer 开启 授权认证服务中心
    	// accessToken 有效期 2小时
    	private static final int ACCESSTOKENVALIDITYSECONDS = 7200;// 两小时
    	private static final int REFRESHTOKENVALIDITYSECONDS = 7200;// 两小时
    	// 配置 appid、appkey 、回调地址、token有效期

    	// 思考问题：accessToken 怎么办？ 使用刷新令牌获取新的accessToken 至少提前10分钟调用刷新令牌接口进行刷新
    	@Override
    	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {

    		// 思考： 如果合作机构需要做oauth2认证的话 第一步操作的是什么？
    		// 1.申请获取到appid 和 appkey 写死的
    		clients.inMemory().withClient("client_1").secret(passwordEncoder().encode("123456"))
    		.redirectUris("http://www.mayikt.com")
    		.authorizedGrantTypes("authorization_code", "password",
    		"refresh_token").scopes("all")
    		.accessTokenValiditySeconds(ACCESSTOKENVALIDITYSECONDS)
    		.refreshTokenValiditySeconds(REFRESHTOKENVALIDITYSECONDS);// 授权类型
    		// 为授权码类型
    		// 密码授权模式 使用用户名称和密码进行获取accessToken
    		// 如果clientid appid 同时使用密码模式和授权code 获取accessToken 为发生什么问题
    		// a 相同
    		// b 覆盖
    		// c 不一致性
    		// d 报错
    		// 密码模式 660fa8e0-f8db-422d-8891-906db021ded8
    		// 授权模式 660fa8e0-f8db-422d-8891-906db021ded8
    		// accessToken 和appid关联

    	}

    	// 设置token类型
    @Override
    	public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
    		endpoints.authenticationManager(authenticationManager()).allowedTokenEndpointRequestMethods(HttpMethod.GET,
    				HttpMethod.POST);
    		// 必须加上他，不然刷新令牌接口会报错
    		endpoints.authenticationManager(authenticationManager());
    		endpoints.userDetailsService(userDetailsService());
    		// 之前的accessToken b55c980c-31f7-4498-a783-bd905608fb18
    		// 刷新之后accessToken d40f7915-dd06-4503-83c8-2815915c9368
    	}

    	@Override
    	public void configure(AuthorizationServerSecurityConfigurer oauthServer) {
    		// 允许表单认证
    		oauthServer.allowFormAuthenticationForClients();
    		// 允许check_token访问
    		oauthServer.checkTokenAccess("permitAll()");
    	}

    	@Bean
    	AuthenticationManager authenticationManager() {
    		AuthenticationManager authenticationManager = new AuthenticationManager() {

    		    @Override
    			public Authentication authenticate(Authentication authentication)  {
    				return daoAuhthenticationProvider().authenticate(authentication);
    			}
    		};
    		return authenticationManager;
    	}

    	@Bean
    	public AuthenticationProvider daoAuhthenticationProvider() {
    		DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
    		daoAuthenticationProvider.setUserDetailsService(userDetailsService());
    		daoAuthenticationProvider.setHideUserNotFoundExceptions(false);
    		daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
    		return daoAuthenticationProvider;
    	}

    	// 设置添加用户信息,正常应该从数据库中读取
    	@Bean
    	UserDetailsService userDetailsService() {
    		InMemoryUserDetailsManager userDetailsService = new InMemoryUserDetailsManager();
    		userDetailsService.createUser(User.withUsername("user_1").password(passwordEncoder().encode("123456"))
    				.authorities("ROLE_USER").build());
    		userDetailsService.createUser(User.withUsername("user_2").password(passwordEncoder().encode("1234567"))
    				.authorities("ROLE_USER").build());
    		return userDetailsService;
    	}

    	@Bean
    	PasswordEncoder passwordEncoder() {
    		// 加密方式
    		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    		return passwordEncoder;
    	}
}