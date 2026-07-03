package pl.zpo.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the Generic Resource &amp; Request Management System.
 *
 * <p>This is a domain-agnostic system: users create <em>requests</em> against
 * <em>resources</em>, the backend evaluates availability/collision/capacity and runs a
 * domain algorithm, then persists the result. Adapt it to any subject (car rental,
 * parcel lockers, cinema, vet, room booking, ...) by editing configuration and the
 * algorithm — not by renaming files.</p>
 */
@SpringBootApplication
public class ZpoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZpoApplication.class, args);
    }
}
