package api.flightservice.model.specification;

import org.springframework.data.jpa.domain.Specification;
import api.flightservice.model.Flight;
import jakarta.persistence.criteria.*;


public class FlightSpecification implements Specification<Flight> {

    private SearchCriteria criteria;

    public FlightSpecification(final SearchCriteria criteria) {
        this.criteria = criteria;
    }

    @Override
    public Predicate toPredicate(Root<Flight> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        if (criteria.getOperation().equalsIgnoreCase(">")) {
            return builder.greaterThanOrEqualTo(
              root.<String> get(criteria.getKey()), criteria.getValue().toString());
        } 
        else if (criteria.getOperation().equalsIgnoreCase("<")) {
            return builder.lessThanOrEqualTo(
              root.<String> get(criteria.getKey()), criteria.getValue().toString());
        } 
        else if (criteria.getOperation().equalsIgnoreCase(":")) {
            if (root.get(criteria.getKey()).getJavaType() == String.class) {
                return builder.like(
                  root.<String>get(criteria.getKey()), "%" + criteria.getValue() + "%");
            } else {
                return builder.equal(root.get(criteria.getKey()), criteria.getValue());
            }
        }
        return null;
    }
}
