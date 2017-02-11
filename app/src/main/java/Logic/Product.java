package Logic;

import android.graphics.Bitmap;

/**
 * Created by Shlomi on 30/01/2017.
 */

public class Product {

    private String theSeller;
    private String category;
    private String subCategory;
    private String description;
    private String image;
    private  int currentPrice;
    private String title;


    public Product(String theSeller, String category, String subCategory, String description, String image, int currentPrice, String title) {
        this.theSeller = theSeller;
        this.category = category;
        this.subCategory = subCategory;
        this.description = description;
        this.image = image;
        this.currentPrice = currentPrice;
        this.title = title;
    }

    public String getTheSeller() {
        return theSeller;
    }

    public void setTheSeller(String theSeller) {
        this.theSeller = theSeller;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(int currentPrice) {
        this.currentPrice = currentPrice;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
