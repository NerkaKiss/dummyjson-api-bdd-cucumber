package models.request;

import java.util.List;

public record CartUpdateRequest(List<CartProductRequest> products) {
}
