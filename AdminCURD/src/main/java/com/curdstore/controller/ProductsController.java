package com.curdstore.controller;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.curdstore.dao.ProductRepo;
import com.curdstore.models.ProductDto;
import com.curdstore.models.Products;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/products")
public class ProductsController {

	@Autowired
	private ProductRepo repo; 
	
	@GetMapping("")
	public String showproducts(Model model) {
		List<Products> products = repo.findAll();
		System.out.println("Products Fetched: " + products.size()); 
		model.addAttribute("products", products);
		
		return "products/index";
		
	}
	
	@GetMapping("/create")
	public String showcreatePage(Model model) {
		ProductDto productdto = new ProductDto();
		model.addAttribute("productdto", productdto);
		
		return "products/CreateProduct";
		
	}
	
	@PostMapping("/create")
	public String createProduct(@Valid @ModelAttribute("productdto") ProductDto productdto, BindingResult result) {

		if(productdto.getImageFile().isEmpty()) {
			result.addError(new FieldError("productdto", "imageFile", "The Product image is required"));
		}
		if(result.hasErrors()) {
			return "products/CreateProduct";
		}
		
		MultipartFile image = productdto.getImageFile();
		Date createAt = new Date();
		String imageFilename = image.getOriginalFilename();
		
		try {
			String uploadDir = "public/img/";
			Path uploadPath = Paths.get(uploadDir);
			
			if(!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath);
			}
			
			try(InputStream inputStream = image.getInputStream()) {
				
				Files.copy(inputStream, Paths.get(uploadDir + imageFilename), StandardCopyOption.REPLACE_EXISTING);
			}
			
		}
		catch(Exception e) {
			System.out.println("Exception" + e.getMessage());
		}
		

		Products product = new Products();
		product.setName(productdto.getName());
		product.setBrand(productdto.getBrand());
		product.setCategory(productdto.getCategory());
		product.setPrice(productdto.getPrice());
		product.setDescription(productdto.getDescription());
		product.setCreateat(createAt);
		product.setImagefileName(imageFilename);
		
		repo.save(product);
		
		return "redirect:/products";
	}
	
	@GetMapping("/edit")
	public String showEditpage(Model model, @RequestParam int id) {
		
		try {
			
			Products products = repo.findById(id).get();
			
			model.addAttribute("products", products);
			
			
			ProductDto productdto = new ProductDto();
			productdto.setName(products.getName());
			productdto.setBrand(products.getBrand());
			productdto.setCategory(products.getCategory());
			productdto.setDescription(products.getDescription());
			productdto.setPrice(products.getPrice());
			
			model.addAttribute("productdto", productdto);
		}
		catch(Exception ex) {
			System.out.println("Exception"+ ex.getMessage());
			return "redirect:/product";
		}
		
		return "products/EditProduct";
	}
	
	
	
	@PostMapping("/edit")
	public String editProduct( Model model, @RequestParam int id,
			@Valid @ModelAttribute("productdto") ProductDto productdto, 
			BindingResult result ) {

		try {
			Products products = repo.findById(id).get();
			model.addAttribute("products", products);
			
			if(result.hasErrors()) {
				return "products/EditProduct";
			}
			
			if(!productdto.getImageFile().isEmpty()) {
				//delete old image
				String uploadDir = "public/img/";
				Path Oldimage = Paths.get(uploadDir + products.getImagefileName());
				
				try {
					Files.delete(Oldimage);
			    }
				catch(Exception ex) {
					System.out.println("Excpetion" + ex.getMessage());
				}

			//Saving new image
				MultipartFile image = productdto.getImageFile();
				Date createAt = new Date();
				String imageFilename = image.getOriginalFilename();
				try(InputStream inputStream = image.getInputStream()) {
					
					Files.copy(inputStream, Paths.get(uploadDir + imageFilename), StandardCopyOption.REPLACE_EXISTING);
				}
				products.setImagefileName(imageFilename);
				products.setCreateat(createAt);

		}
			products.setName(productdto.getName());
			products.setBrand(productdto.getBrand());
			products.setCategory(productdto.getCategory());
			products.setDescription(productdto.getDescription());
			products.setPrice(productdto.getPrice());
			
			repo.save(products);
		}
		catch(Exception e3) {
			System.out.println("Exception " + e3.getMessage());
		}
		
		return "redirect:/products";
	}
	
	@GetMapping("/delete")
	public String deleteProduct(@RequestParam int id) {

		try {
			Products products = repo.findById(id).get();
			
			Path imagePath = Paths.get("public/img/" + products.getImagefileName());
			
			try {
				Files.delete(imagePath);
			}
			catch(Exception ex) {
				System.out.println("Exceptions " + ex.getMessage());
			}
			
			repo.delete(products);
			
		}
		catch(Exception ex) {
			System.out.println("Exception " + ex.getMessage());
		}
		return "redirect:/products";
	}
}

