package Logic;


import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Shlomi on 31/01/2017.
 */

public class Sell {
    private Product theProduct;
    private Date createDate;
    private String allOffers;
    private String id;

    public Sell(Product theProduct, Date createDate, String allOffers, String id) {
        this.theProduct = theProduct;
        this.createDate = createDate;
        this.allOffers = allOffers;
        this.id = id;
    }

    public Product getTheProduct() {
        return theProduct;
    }

    public void setTheProduct(Product theProduct) {
        this.theProduct = theProduct;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getAllOffers() {
        return allOffers;
    }

    public void setAllOffers(String allOffers) {
        this.allOffers = allOffers;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
