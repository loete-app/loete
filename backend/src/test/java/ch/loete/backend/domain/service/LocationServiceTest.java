package ch.loete.backend.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import ch.loete.backend.process.repository.LocationRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

  @Mock private LocationRepository locationRepository;

  @InjectMocks private LocationService locationService;

  @Test
  void getCities_returnsCitiesFromRepository() {
    given(locationRepository.findDistinctCitiesWithEvents())
        .willReturn(List.of("Basel", "Bern", "Zurich"));

    List<String> result = locationService.getCities();

    assertThat(result).containsExactly("Basel", "Bern", "Zurich");
  }

  @Test
  void getCities_returnsEmptyListWhenNoCities() {
    given(locationRepository.findDistinctCitiesWithEvents()).willReturn(List.of());

    List<String> result = locationService.getCities();

    assertThat(result).isEmpty();
  }
}
