package lk.serenetravels.userservice.controller;

import jakarta.validation.Valid;
import lk.serenetravels.userservice.dto.CreateUserRequest;
import lk.serenetravels.userservice.dto.RegisterUserRequest;
import lk.serenetravels.userservice.dto.UserResponse;
import lk.serenetravels.userservice.entity.UserType;
import lk.serenetravels.userservice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Public self-service signup: always a CUSTOMER, password usable immediately,
    // but left unverified until an admin confirms the account.
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        CreateUserRequest createRequest = new CreateUserRequest();
        createRequest.setUsername(request.getUsername());
        createRequest.setEmail(request.getEmail());
        createRequest.setFirstName(request.getFirstName());
        createRequest.setLastName(request.getLastName());
        createRequest.setPassword(request.getPassword());
        createRequest.setUserType(UserType.CUSTOMER);

        UserResponse response = userService.createUser(createRequest, false, false);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Admin-driven creation: any user type, requires an authenticated caller,
    // and considered verified immediately since an admin vetted it directly.
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.createUser(request, true, true);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Admin confirms a self-registered account.
    @PatchMapping("/{id}/verify")
    public ResponseEntity<UserResponse> verify(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.verifyUser(id));
    }
}
