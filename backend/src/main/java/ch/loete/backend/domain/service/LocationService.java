package ch.loete.backend.domain.service;

import ch.loete.backend.process.repository.LocationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;

    @Transactional(readOnly = true)
    public List<String> getCities() {
        return locationRepository.findDistinctCitiesWithEvents();
    }
}
