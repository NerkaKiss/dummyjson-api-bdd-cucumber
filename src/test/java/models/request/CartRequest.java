package models.request;

import java.util.List;

public record CartRequest(int userId, List<CartProductRequest> products) {
}
