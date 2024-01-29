package com.ecommerce.project.service;

import com.ecommerce.project.dto.AddressDTO;
import com.ecommerce.project.entity.Address;
import com.ecommerce.project.entity.User;
import com.ecommerce.project.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    public List<Address> getAddressesForUser(User user) {
        return addressRepository.findByUserAndDeletedFalse(user);
    }

    public void saveAddress(AddressDTO addressDTO, User user) {

        Address address = new Address();
        address.setUser(user);
        address.setStreetAddress(addressDTO.getStreetAddress());
        address.setCity(addressDTO.getCity());
        address.setState(addressDTO.getState());
        address.setPostalCode(addressDTO.getPostalCode());
        addressRepository.save(address);

    }

    public boolean deleteAddressById(Long id) {

        Optional<Address> addressOptional = addressRepository.findById(id);
        if (addressOptional.isEmpty())
            return false;

        try {
            addressRepository.deleteById(id);
        } catch (Exception e) {
            Address address = addressOptional.get();
            address.setDeleted(true);
            address.setUser(null);
            addressRepository.save(address);
        }

        return true;
    }

    public Address getAddressById(Long selectedAddressId) {
        return addressRepository.findById(selectedAddressId).get();
    }

}
