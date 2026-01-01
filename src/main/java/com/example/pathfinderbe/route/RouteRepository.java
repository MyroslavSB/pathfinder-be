package com.example.pathfinderbe.route;

import com.example.pathfinderbe.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RouteRepository extends JpaRepository<Route, Long> {

    List<Route> findAllByUser(User user);
}
