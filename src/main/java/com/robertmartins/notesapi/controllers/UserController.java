package com.robertmartins.notesapi.controllers;

import com.robertmartins.notesapi.dtos.UserCredentialsDto;
import com.robertmartins.notesapi.dtos.UserDto;
import com.robertmartins.notesapi.models.UserModel;
import com.robertmartins.notesapi.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private ProfileController profileController;

    @Autowired
    private AddressController addressController;

    @PostMapping
    public ResponseEntity<Object> save(@RequestBody @Valid UserDto userDto){
        if(userService.existsByLogin(userDto.getProfile().getEmail()))
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Email already in use");
        var user = new UserModel();
        var address = addressController.setAddress(userDto.getProfile().getAddress());
        var profile = profileController.setProfile(userDto.getProfile(), address);
        user.setLogin(userDto.getProfile().getEmail());
        user.setPassword(userDto.getPassword());
        user.setProfile(profile);
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.save(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable(name = "id") int id){
        var user = userService.findById(id);
        if(user.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateUserCredentialsById(@PathVariable(name = "id") int id, @RequestBody @Valid UserCredentialsDto userCredentials){
        var user = userService.findById(id);
        if(user.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        user.get().setLogin(userCredentials.getLogin());
        user.get().setPassword(userCredentials.getPassword());
        user.get().setUpdatedAt(new Date());
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.save(user.get()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteUserById(@PathVariable(name = "id") int id){
        userService.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK).body("User deleted");
    }

}
