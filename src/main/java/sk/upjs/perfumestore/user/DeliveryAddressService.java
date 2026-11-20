package sk.upjs.perfumestore.user;

import sk.upjs.perfumestore.exception.BadRequestException;

import java.util.List;

public class DeliveryAddressService {

    private final DeliveryAddressDAO deliveryAddressDAO;

    public DeliveryAddressService(DeliveryAddressDAO deliveryAddressDAO) {
        this.deliveryAddressDAO = deliveryAddressDAO;
    }
    public List<DeliveryAddress> findByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return deliveryAddressDAO.findByUserId(userId);
    }

    public DeliveryAddress create(Long userId, String street,String city,String postalCode,String country) {
        if(userId == null){
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if(street == null || street.isBlank() || city == null || city.isBlank() || postalCode == null || postalCode.isBlank() || country == null || country.isBlank()){
            throw new BadRequestException("All address fields must be filled");
        }

        String cleanStreet = street.trim();
        String cleanCity = city.trim();
        String cleanPostalCode = postalCode.trim();
        String cleanCountry = country.trim();

        List<DeliveryAddress> deliveryAddresses = deliveryAddressDAO.findByUserId(userId);

        for(DeliveryAddress address: deliveryAddresses){
            if(address.street().equalsIgnoreCase(cleanStreet)
            && address.city().equalsIgnoreCase(cleanCity)
            && address.country().equalsIgnoreCase(cleanCountry)
            && address.postalCode().equalsIgnoreCase(cleanPostalCode)){
                throw new IllegalArgumentException("Address already exists");
            }
        }

        DeliveryAddress newAddress = new DeliveryAddress(
                null,
                userId,
                cleanStreet,
                cleanCity,
                cleanPostalCode,
                cleanCountry
        );
        return deliveryAddressDAO.create(newAddress);
    }
}
