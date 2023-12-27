package com.example.security.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {


    private final JwtService jwtService;
    
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }


    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {

       //filter operations
       //get the authorization header
       final String authHeader = request.getHeader("Authorization");
       final String jwt;
       final String userEmail;

       //check for the token
       if(authHeader == null || !authHeader.startsWith(("Bearer "))){
           filterChain.doFilter(request, response);
           return;
       }

       //extract the token if present
       jwt = authHeader.substring(7);
       //extract the user email
       userEmail = jwtService.extractUsername(jwt);

       //check if user email is not empty and user is not already authenticated
       if(userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null){
           UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
           if(jwtService.isTokenValid(jwt, userDetails)){

               UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                       userDetails, null, userDetails.getAuthorities()
               );

               authenticationToken.setDetails(
                       new WebAuthenticationDetailsSource().buildDetails(request)
               );

               SecurityContextHolder.getContext().setAuthentication(authenticationToken);

           }


       }

       filterChain.doFilter(request, response);

    }
}
