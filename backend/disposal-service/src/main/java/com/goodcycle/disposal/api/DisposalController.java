package com.goodcycle.disposal.api;

import java.util.List;

import com.goodcycle.disposal.event.DisposalEventPublisher;
import com.goodcycle.disposal.model.DisposalMethod;
import com.goodcycle.disposal.model.DisposalRequest;
import com.goodcycle.disposal.model.DisposalRequestRepository;
import com.goodcycle.disposal.model.DisposalStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/disposals")
public class DisposalController {
    private final DisposalRequestRepository requests;
    private final DisposalEventPublisher events;

    public DisposalController(DisposalRequestRepository requests, DisposalEventPublisher events) {
        this.requests = requests;
        this.events = events;
    }

    @GetMapping
    public List<DisposalResponse> list(Authentication authentication) {
        String role = role(authentication);
        List<DisposalRequest> found = isStaff(role)
                ? requests.findAllByOrderByCreatedAtDesc()
                : requests.findByCustomerPhoneOrderByCreatedAtDesc(authentication.getName());
        return found.stream().map(DisposalResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DisposalResponse create(
            @Valid @RequestBody CreateRequest body,
            JwtAuthenticationToken authentication) {
        DisposalRequest saved = requests.save(new DisposalRequest(
                authentication.getToken().getSubject(),
                body.customerName().trim(),
                body.medicineType(),
                body.quantity(),
                body.method(),
                body.location().trim(),
                body.note() == null ? null : body.note().trim()));
        events.publish(saved);
        return DisposalResponse.from(saved);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPPORT', 'ADMIN')")
    public DisposalResponse updateStatus(
            @PathVariable("id") String id,
            @Valid @RequestBody UpdateStatus body) {
        DisposalRequest request = requests.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found."));
        request.updateStatus(body.status());
        DisposalRequest saved = requests.save(request);
        events.publish(saved);
        return DisposalResponse.from(saved);
    }

    private static String role(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring(5))
                .findFirst()
                .orElse("CUSTOMER");
    }

    private static boolean isStaff(String role) {
        return role.equals("SUPPORT") || role.equals("ADMIN");
    }

    public record CreateRequest(
            @NotBlank @Size(max = 100) String customerName,
            @NotBlank @Size(max = 40) String medicineType,
            @NotBlank @Size(max = 40) String quantity,
            @NotNull DisposalMethod method,
            @NotBlank @Size(max = 120) String location,
            @Size(max = 500) String note) {}

    public record UpdateStatus(@NotNull DisposalStatus status) {}

    public record DisposalResponse(
            String id,
            String customerPhone,
            String customerName,
            String medicineType,
            String quantity,
            DisposalMethod method,
            String location,
            String note,
            DisposalStatus status,
            java.time.Instant createdAt,
            java.time.Instant updatedAt) {
        static DisposalResponse from(DisposalRequest request) {
            return new DisposalResponse(
                    request.getId(), request.getCustomerPhone(), request.getCustomerName(),
                    request.getMedicineType(), request.getQuantity(), request.getMethod(),
                    request.getLocation(), request.getNote(), request.getStatus(),
                    request.getCreatedAt(), request.getUpdatedAt());
        }
    }
}
