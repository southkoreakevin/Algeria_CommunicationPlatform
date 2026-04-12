package com.example.demo.repository.memory;

import lombok.Getter;

@Getter
public class ItemSearchDto {

    private String email;

    public ItemSearchDto (String email){
        this.email = email;
    }
}
