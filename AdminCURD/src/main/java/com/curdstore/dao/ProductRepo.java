package com.curdstore.dao;

import org.springframework.data.jpa.repository.JpaRepository;


import com.curdstore.models.Products;

public interface ProductRepo extends JpaRepository<Products, Integer> {

}
