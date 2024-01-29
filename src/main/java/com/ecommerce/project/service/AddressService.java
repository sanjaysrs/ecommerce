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
        if (addressDTO.getId()!=null)
            address.setId(addressDTO.getId());
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

    public Address getAddressById(Long id) {
        Optional<Address> addressOptional = addressRepository.findByIdAndDeletedFalse(id);
        Address address = addressOptional.orElse(null);
        return address;
    }

    public AddressDTO getAddressDTOById(Long id, User user) {
        Optional<Address> addressOptional = addressRepository.findByIdAndDeletedFalse(id);
        Address address = addressOptional.orElse(new Address());

        if (!address.getUser().equals(user))
            return new AddressDTO();

        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setStreetAddress(address.getStreetAddress());
        addressDTO.setCity(address.getCity());
        addressDTO.setState(address.getState());
        addressDTO.setPostalCode(address.getPostalCode());
        return addressDTO;

    }

}
