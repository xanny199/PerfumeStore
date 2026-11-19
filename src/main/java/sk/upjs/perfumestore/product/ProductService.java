package sk.upjs.perfumestore.product;

import java.util.ArrayList;
import java.util.List;

public class ProductService {

    private final ProductDAO productDAO;

    public ProductService(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    public List<Product> getAllAvailableProducts() {
        List<Product> resultList = new ArrayList<>();
        for (Product p : productDAO.findAll()) {
            if (p.isAvailable() && p.currentStock() != null && p.currentStock() > 0) {
                resultList.add(p);
            }
        }
        return resultList;
    }
}
