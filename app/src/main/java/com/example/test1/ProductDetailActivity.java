package com.example.test1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.test1.dao.ProductDAO;
import com.example.test1.entity.CartItem;
import com.example.test1.entity.Product;

public class ProductDetailActivity extends AppCompatActivity {
    private static final String TAG = "ProductDetailActivity";
    public static final String EXTRA_PRODUCT_ID = "extra_product_id";

    private ImageView detailImageProduct;
    private TextView detailTextProductName, detailTextPrice, detailTextSales;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        Log.d(TAG, "onCreate called");

        detailImageProduct = findViewById(R.id.detailImageProduct);
        detailTextProductName = findViewById(R.id.detailTextProductName);
        detailTextPrice = findViewById(R.id.detailTextPrice);
        detailTextSales = findViewById(R.id.detailTextSales);
        ImageButton backBtn = findViewById(R.id.backBtn);

        Intent intent = getIntent();
        int productId = intent.getIntExtra(EXTRA_PRODUCT_ID, -1);
        if (productId == -1) {
            Log.e(TAG, "Invalid product ID received");
            Toast.makeText(this, "Invalid product selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ProductDAO productDAO = new ProductDAO(this);
        Product product = productDAO.getProduct(productId);
        if (product == null) {
            Log.e(TAG, "Product not found for ID: " + productId);
            Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            displayProductDetails(product);
            Log.d(TAG, "Product details displayed for: " + product.getProductName());
        } catch (Exception e) {
            Log.e(TAG, "Failed to display product details: " + e.getMessage(), e);
            finish(); // Close if display fails
        }
        backBtn.setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked, navigating back");
            onBackPressed(); // Call the default back press behavior
        });
        Button addToCartButton = findViewById(R.id.addToCartButton);
        addToCartButton.setOnClickListener(v -> {
            try {
                if (product.getUnitQuantity() > 0) {
                    CartItem item = new CartItem(product, 1, false);
                    ShoppingCartManager.getInstance().addCartItem(item);
                    Toast.makeText(this, product.getProductName() + " added to cart", Toast.LENGTH_SHORT).show();
                    
                    // Update product quantity
                    product.setUnitQuantity(product.getUnitQuantity() - 1);
                    productDAO.updateProduct(product);
                    displayProductDetails(product);
                    
                    // Show option to view cart
                    new AlertDialog.Builder(this)
                        .setTitle("Added to Cart")
                        .setMessage("Would you like to view your cart?")
                        .setPositiveButton("View Cart", (dialog, which) -> 
                            startActivity(new Intent(ProductDetailActivity.this, ShoppingCartActivity.class)))
                        .setNegativeButton("Continue Shopping", null)
                        .show();
                } else {
                    Toast.makeText(this, "Sorry, this item is out of stock", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error adding item to cart: " + e.getMessage(), e);
                Toast.makeText(this, "Failed to add item to cart", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayProductDetails(Product product) {
        if (product != null) {
            detailTextProductName.setText(product.getProductName());
            detailTextPrice.setText(String.format("$%.2f", product.getUnitPrice()));
            detailTextSales.setText(String.format("Sales: %d | Stock: %d", product.getSales(), product.getUnitQuantity()));
            detailTextDescription.setText(product.getProductDescription());
            
            try {
                detailImageProduct.setImageResource(product.getImageResId());
            } catch (Exception e) {
                Log.e(TAG, "Failed to set image", e);
                detailImageProduct.setImageResource(android.R.drawable.ic_menu_help);
            }
            
            // Disable add to cart button if out of stock
            addToCartButton.setEnabled(product.getUnitQuantity() > 0);
            addToCartButton.setText(product.getUnitQuantity() > 0 ? "Add to Cart" : "Out of Stock");
        }
    }
}