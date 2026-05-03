package com.example.demo.config;

import com.example.demo.dto.AuthResponse;
import com.example.demo.entity.UserAccount;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.typeMap(UserAccount.class, AuthResponse.class)
                .addMappings(m -> m.map(UserAccount::getId, AuthResponse::setUserId));
        return mapper;
    }
}

