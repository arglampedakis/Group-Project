/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.PetKeeper.controller;

import com.example.PetKeeper.dto.AddressDto;
import com.example.PetKeeper.model.Address;
import com.example.PetKeeper.model.MyUser;
import com.example.PetKeeper.service.AddressService;
import com.example.PetKeeper.service.KeepersAvailabilityService;
import com.example.PetKeeper.service.MyUserService;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author RG
 */
@RestController
public class AddressController {

    @Autowired
    MyUserService myUserService;
    @Autowired
    AddressService addressService;
    @Autowired
    KeepersAvailabilityService keepersAvailabilityService;

    @PostMapping("/keeper/registerMyAddress")
    public Address registerAddress(
            @RequestParam("latitude") BigDecimal latitude,
            @RequestParam("longitude") BigDecimal longitude,
            @RequestParam("country") String country,
            @RequestParam("city") String city,
            @RequestParam("streetName") String streetName,
            @RequestParam("streetNumber") String streetNumber,
            @RequestParam("postalCode") String postalCode,
            @RequestParam("price") BigDecimal price,
            Principal principal) {

        MyUser loggedInMyUser = myUserService
                .getMyUserByUsername(principal.getName());

        Address myAddress = new Address();

        myAddress.setMyUserId(loggedInMyUser);
        myAddress.setCountry(country);
        myAddress.setCity(city);
        myAddress.setStreetName(streetName);
        myAddress.setStreetNumber(streetNumber);
        myAddress.setPostalCode(postalCode);
        myAddress.setPrice(price);
        myAddress.setLatitude(latitude);
        myAddress.setLongitude(longitude);

        if (loggedInMyUser.getAddress() != null) {
            myAddress.setAddressId(loggedInMyUser.getAddress().getAddressId());
            //update address info
            addressService.saveAddress(myAddress);
        } else {
            //register new address
            addressService.saveAddress(myAddress);
        }

        myAddress.setMyUserId(null);
        return myAddress;
    }

    @GetMapping("/keeper/myAddress")
    public Address getMyAddress(Principal principal) {

        MyUser loggedInMyUser = myUserService
                .getMyUserByUsername(principal.getName());

        Address myAddress = new Address();

        if (loggedInMyUser.getAddress() != null) {
            myAddress = loggedInMyUser.getAddress();
            myAddress.setMyUserId(null);
            return myAddress;
        } else {
            return null;
        }

    }

    @GetMapping("/owner/findKeepers")
    public List<AddressDto> findKeepers(@RequestParam("latitude") BigDecimal latitude,
            @RequestParam("longitude") BigDecimal longitude,
            @RequestParam("fromDate") Date fromDate,
            @RequestParam("toDate") Date toDate
    ) {

        List<Address> addresses = addressService.getAllByLngLatWithinRadius(latitude, longitude);

        List<Address> filteredAddresses = keepersAvailabilityService.filterAddressListBasedOnAvailability(addresses, fromDate, toDate);

        List<AddressDto> result = new ArrayList<>();

        for (Address address : filteredAddresses) {
            if (null != address.getMyUserId().getMyUserDetails()) {
                AddressDto addressDto = new AddressDto();
                addressDto.fillDtoFromAddress(address);
                result.add(addressDto);
            }
        }
        return result;
    }
}
