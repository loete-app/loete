package ch.loete.backend.config;

import ch.loete.backend.domain.model.Category;
import ch.loete.backend.domain.model.Event;
import ch.loete.backend.domain.model.Location;
import ch.loete.backend.process.repository.CategoryRepository;
import ch.loete.backend.process.repository.EventRepository;
import ch.loete.backend.process.repository.LocationRepository;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevDataSeeder implements ApplicationRunner {

    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (eventRepository.count() > 0) {
            log.info("Dev seed data already exists, skipping.");
            return;
        }

        log.info("Seeding dev data...");

        List<Location> locations = seedLocations();
        List<Event> events = seedEvents(locations);

        log.info("Dev seed data complete: {} locations, {} events", locations.size(), events.size());
    }

    private List<Location> seedLocations() {
        List<Location> locations =
                List.of(
                        Location.builder()
                                .name("Hallenstadion")
                                .city("Zürich")
                                .latitude(47.4111)
                                .longitude(8.5514)
                                .build(),
                        Location.builder()
                                .name("Kaufleuten")
                                .city("Zürich")
                                .latitude(47.3735)
                                .longitude(8.5318)
                                .build(),
                        Location.builder()
                                .name("Tonhalle")
                                .city("Zürich")
                                .latitude(47.3656)
                                .longitude(8.5390)
                                .build(),
                        Location.builder()
                                .name("Komplex 457")
                                .city("Zürich")
                                .latitude(47.3900)
                                .longitude(8.5048)
                                .build(),
                        Location.builder()
                                .name("Hive Club")
                                .city("Zürich")
                                .latitude(47.3886)
                                .longitude(8.5190)
                                .build(),
                        Location.builder()
                                .name("Zukunft")
                                .city("Zürich")
                                .latitude(47.3870)
                                .longitude(8.5040)
                                .build(),
                        Location.builder()
                                .name("Bar am Wasser")
                                .city("Zürich")
                                .latitude(47.3660)
                                .longitude(8.5410)
                                .build(),
                        Location.builder()
                                .name("Dachkantine")
                                .city("Zürich")
                                .latitude(47.3905)
                                .longitude(8.5055)
                                .build(),
                        Location.builder()
                                .name("Bierhübeli")
                                .city("Bern")
                                .latitude(46.9480)
                                .longitude(7.4380)
                                .build(),
                        Location.builder()
                                .name("Kaserne Basel")
                                .city("Basel")
                                .latitude(47.5580)
                                .longitude(7.5830)
                                .build(),
                        Location.builder()
                                .name("Südpol")
                                .city("Luzern")
                                .latitude(47.0350)
                                .longitude(8.2990)
                                .build(),
                        Location.builder()
                                .name("Les Docks")
                                .city("Lausanne")
                                .latitude(46.5087)
                                .longitude(6.5978)
                                .build());
        return locationRepository.saveAll(locations);
    }

    private List<Event> seedEvents(List<Location> locations) {
        Map<String, Location> loc = new HashMap<>();
        for (Location l : locations) {
            loc.put(l.getName(), l);
        }

        Category konzert = categoryRepository.findBySlug("konzert").orElseThrow();
        Category festival = categoryRepository.findBySlug("festival").orElseThrow();
        Category sonstiges = categoryRepository.findBySlug("sonstiges").orElseThrow();

        LocalDateTime now = LocalDateTime.now();

        List<Event> events = new ArrayList<>();

        // --- Konzerte ---
        events.add(
                event(
                        "Patent Ochsner – Live",
                        "Die Berner Kultband Patent Ochsner live auf Tour. Erlebt Songs wie"
                            + " «Scharlachrot» und «W. Nuss vo Bümpliz» in einzigartiger"
                            + " Atmosphäre.",
                        "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=800&h=500&fit=crop",
                        now.plusDays(5).with(LocalTime.of(20, 0)),
                        now.plusDays(5).with(LocalTime.of(23, 0)),
                        konzert,
                        loc.get("Hallenstadion")));

        events.add(
                event(
                        "Stress – Concert",
                        "Der Westschweizer Rapper Stress begeistert mit energiegeladenen Beats"
                                + " und gesellschaftskritischen Texten.",
                        "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=800&h=500&fit=crop",
                        now.plusDays(12).with(LocalTime.of(20, 0)),
                        now.plusDays(12).with(LocalTime.of(23, 0)),
                        konzert,
                        loc.get("Kaufleuten")));

        events.add(
                event(
                        "Lo & Leduc – Abschiedstour",
                        "Lo & Leduc verabschieden sich mit einer letzten Tour. Hits wie «079»"
                                + " und «Jung verdammt» noch einmal live erleben.",
                        "https://images.unsplash.com/photo-1459749411175-04bf5292ceea?w=800&h=500&fit=crop",
                        now.plusDays(20).with(LocalTime.of(20, 0)),
                        now.plusDays(20).with(LocalTime.of(23, 0)),
                        konzert,
                        loc.get("Komplex 457")));

        events.add(
                event(
                        "Jazz Night Tonhalle",
                        "Internationale und Schweizer Jazzkünstler für einen Abend voller"
                                + " Improvisation und Groove in der Tonhalle.",
                        "https://images.unsplash.com/photo-1511192336575-5a79af67a629?w=800&h=500&fit=crop",
                        now.plusDays(30).with(LocalTime.of(20, 0)),
                        now.plusDays(30).with(LocalTime.of(23, 0)),
                        konzert,
                        loc.get("Tonhalle")));

        events.add(
                event(
                        "Sophie Hunger – Acoustic",
                        "Sophie Hunger präsentiert ihr neues Album in intimem Akustik-Setting."
                                + " Melancholisch, poetisch, unvergesslich.",
                        "https://images.unsplash.com/photo-1514320291840-2e0a9bf2a9ae?w=800&h=500&fit=crop",
                        now.plusDays(8).with(LocalTime.of(20, 30)),
                        now.plusDays(8).with(LocalTime.of(23, 0)),
                        konzert,
                        loc.get("Bierhübeli")));

        events.add(
                event(
                        "Pegasus – Tour",
                        "Das Berner Trio Pegasus bringt Deutschpop-Hits wie «Elefant» und"
                                + " «Bettler und Prinz» auf die grosse Bühne.",
                        "https://images.unsplash.com/photo-1524368535928-5b5e00ddc76b?w=800&h=500&fit=crop",
                        now.plusDays(17).with(LocalTime.of(20, 0)),
                        now.plusDays(17).with(LocalTime.of(23, 0)),
                        konzert,
                        loc.get("Kaserne Basel")));

        events.add(
                event(
                        "Hecht – Clubtour",
                        "Die Zürcher Band Hecht mit ihren Mundart-Hits live. Party garantiert"
                                + " bei «Züri» und «Cocktail».",
                        "https://images.unsplash.com/photo-1540039155733-5bb30b53aa14?w=800&h=500&fit=crop",
                        now.plusDays(25).with(LocalTime.of(21, 0)),
                        now.plusDays(25).with(LocalTime.of(23, 30)),
                        konzert,
                        loc.get("Kaufleuten")));

        events.add(
                event(
                        "Myss Keta – Live in Zürich",
                        "Die italienische Elektropop-Ikone Myss Keta erstmals live in Zürich."
                                + " Masken, Beats und Provokation.",
                        "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=800&h=500&fit=crop",
                        now.plusDays(35).with(LocalTime.of(21, 0)),
                        now.plusDays(35).with(LocalTime.of(23, 30)),
                        konzert,
                        loc.get("Komplex 457")));

        events.add(
                event(
                        "Stephan Eicher – Retrospektive",
                        "Der Bieler Chansonnier spielt Klassiker aus vier Jahrzehnten. Von"
                                + " «Déjeuner en paix» bis «Combien de temps».",
                        "https://images.unsplash.com/photo-1415201364774-f6f0bb35f28f?w=800&h=500&fit=crop",
                        now.plusDays(42).with(LocalTime.of(20, 0)),
                        now.plusDays(42).with(LocalTime.of(22, 30)),
                        konzert,
                        loc.get("Les Docks")));

        events.add(
                event(
                        "Nemo – Eurovisionsparty",
                        "Nemo feiert den ESC-Sieg mit einer exklusiven Heimshow. Pop, Rap und"
                                + " nonbinäre Energie auf der Bühne.",
                        "https://images.unsplash.com/photo-1492684223f8-e1f1362e1e0b?w=800&h=500&fit=crop",
                        now.plusDays(48).with(LocalTime.of(20, 0)),
                        now.plusDays(48).with(LocalTime.of(23, 0)),
                        konzert,
                        loc.get("Hallenstadion")));

        // --- Festivals ---
        events.add(
                event(
                        "Zürich Openair",
                        "Das grösste Open-Air-Festival der Stadt Zürich. Drei Tage Musik, Food"
                                + " und Kultur unter freiem Himmel.",
                        "https://images.unsplash.com/photo-1533174072545-7a4b6ad7a6c3?w=800&h=500&fit=crop",
                        now.plusDays(40).with(LocalTime.of(14, 0)),
                        now.plusDays(42).with(LocalTime.of(2, 0)),
                        festival,
                        loc.get("Komplex 457")));

        events.add(
                event(
                        "Fête de la Musique Lausanne",
                        "Lausanne feiert die Musik! Kostenlose Konzerte auf Plätzen und"
                                + " Strassen der ganzen Stadt.",
                        "https://images.unsplash.com/photo-1429962714451-bb934ecdc4ec?w=800&h=500&fit=crop",
                        now.plusDays(55).with(LocalTime.of(14, 0)),
                        now.plusDays(57).with(LocalTime.of(2, 0)),
                        festival,
                        loc.get("Les Docks")));

        events.add(
                event(
                        "Lethargy Festival",
                        "Elektronische Musik trifft auf Industriecharme. Zwei Tage Techno,"
                                + " House und Ambient im alten Güterbahnhof.",
                        "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=800&h=500&fit=crop",
                        now.plusDays(62).with(LocalTime.of(16, 0)),
                        now.plusDays(63).with(LocalTime.of(6, 0)),
                        festival,
                        loc.get("Zukunft")));

        events.add(
                event(
                        "Montreux Jazz Nights",
                        "Ableger des legendären Montreux Jazz Festivals. Drei Nächte Jazz,"
                                + " Soul und Blues am See.",
                        "https://images.unsplash.com/photo-1470229722913-7c0e2dbbafd3?w=800&h=500&fit=crop",
                        now.plusDays(70).with(LocalTime.of(18, 0)),
                        now.plusDays(72).with(LocalTime.of(1, 0)),
                        festival,
                        loc.get("Les Docks")));

        events.add(
                event(
                        "Basscamp Bern",
                        "Drum & Bass, Jungle und Breakbeat – drei Floors, zwei Tage. Das"
                                + " härteste Bass-Festival der Schweiz.",
                        "https://images.unsplash.com/photo-1504680177321-2e6a879aac86?w=800&h=500&fit=crop",
                        now.plusDays(78).with(LocalTime.of(20, 0)),
                        now.plusDays(79).with(LocalTime.of(6, 0)),
                        festival,
                        loc.get("Bierhübeli")));

        events.add(
                event(
                        "Luzerner Stadtfest",
                        "Musik, Kulinarik und Feuerwerk am Vierwaldstättersee. Das"
                                + " traditionelle Stadtfest in einmaliger Kulisse.",
                        "https://images.unsplash.com/photo-1506157786151-b8491531f063?w=800&h=500&fit=crop",
                        now.plusDays(85).with(LocalTime.of(14, 0)),
                        now.plusDays(87).with(LocalTime.of(2, 0)),
                        festival,
                        loc.get("Südpol")));

        // --- Bar & Club Nights ---
        events.add(
                event(
                        "Techno Tuesday – Hive",
                        "Wöchentliche Techno-Nacht im Hive Club. Residents und Gäste sorgen"
                                + " für harte Beats bis in die Morgenstunden.",
                        "https://images.unsplash.com/photo-1571266028243-e4733b0f0bb0?w=800&h=500&fit=crop",
                        now.plusDays(3).with(LocalTime.of(22, 0)),
                        now.plusDays(4).with(LocalTime.of(5, 0)),
                        sonstiges,
                        loc.get("Hive Club")));

        events.add(
                event(
                        "Vinyl & Cocktails",
                        "Vinyl-DJ-Set mit handverlesenen Drinks. Funk, Soul und Disco auf"
                                + " Schallplatte in gemütlicher Bar-Atmosphäre.",
                        "https://images.unsplash.com/photo-1566417713940-fe7c737a9ef2?w=800&h=500&fit=crop",
                        now.plusDays(6).with(LocalTime.of(19, 0)),
                        now.plusDays(6).with(LocalTime.of(1, 0)),
                        sonstiges,
                        loc.get("Bar am Wasser")));

        events.add(
                event(
                        "House of Zukunft",
                        "Deep House & Minimal im Zukunft. Internationale DJs und lokale"
                                + " Talente auf zwei Floors.",
                        "https://images.unsplash.com/photo-1598387993281-cecf8b71a8f8?w=800&h=500&fit=crop",
                        now.plusDays(10).with(LocalTime.of(23, 0)),
                        now.plusDays(11).with(LocalTime.of(6, 0)),
                        sonstiges,
                        loc.get("Zukunft")));

        events.add(
                event(
                        "Aperitivo Musicale",
                        "After-Work mit Live-Jazz, Aperol Spritz und Antipasti. Entspannter"
                                + " Start ins Wochenende an der Limmat.",
                        "https://images.unsplash.com/photo-1543007630-9710e4a00a20?w=800&h=500&fit=crop",
                        now.plusDays(4).with(LocalTime.of(17, 0)),
                        now.plusDays(4).with(LocalTime.of(22, 0)),
                        sonstiges,
                        loc.get("Bar am Wasser")));

        events.add(
                event(
                        "Dachkantine Sessions",
                        "Live-Bands auf der Rooftop-Bühne. Indie, Alternative und"
                                + " Singer-Songwriter mit Blick über die Gleise.",
                        "https://images.unsplash.com/photo-1485872299829-c44036d58e00?w=800&h=500&fit=crop",
                        now.plusDays(14).with(LocalTime.of(20, 0)),
                        now.plusDays(14).with(LocalTime.of(23, 30)),
                        sonstiges,
                        loc.get("Dachkantine")));

        events.add(
                event(
                        "Karaoke & Bier Nacht",
                        "Singen, trinken, wiederholen. Die legendäre Karaoke-Nacht im"
                                + " Bierhübeli mit über 10'000 Songs zur Auswahl.",
                        "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=800&h=500&fit=crop",
                        now.plusDays(9).with(LocalTime.of(20, 0)),
                        now.plusDays(9).with(LocalTime.of(2, 0)),
                        sonstiges,
                        loc.get("Bierhübeli")));

        events.add(
                event(
                        "Latin Vibes – Salsa Night",
                        "Salsa, Bachata und Reggaeton. Tanzkurs ab 21 Uhr, danach Party mit"
                                + " DJ Carlos bis spät in die Nacht.",
                        "https://images.unsplash.com/photo-1504196606672-aef5c9cefc92?w=800&h=500&fit=crop",
                        now.plusDays(16).with(LocalTime.of(21, 0)),
                        now.plusDays(17).with(LocalTime.of(3, 0)),
                        sonstiges,
                        loc.get("Kaufleuten")));

        events.add(
                event(
                        "Open Decks – Kaserne",
                        "Bring deine eigenen Platten mit! Offene Turntables für alle DJs und"
                                + " Vinyl-Liebhaber. Eintritt frei.",
                        "https://images.unsplash.com/photo-1571935441389-890461a63390?w=800&h=500&fit=crop",
                        now.plusDays(22).with(LocalTime.of(20, 0)),
                        now.plusDays(22).with(LocalTime.of(1, 0)),
                        sonstiges,
                        loc.get("Kaserne Basel")));

        events.add(
                event(
                        "Whisky Tasting & Blues",
                        "Sechs Single Malts begleitet von Live-Blues-Gitarre. Ein Abend für"
                                + " Geniesser im Südpol Luzern.",
                        "https://images.unsplash.com/photo-1569529465841-dfecdab7503b?w=800&h=500&fit=crop",
                        now.plusDays(28).with(LocalTime.of(19, 0)),
                        now.plusDays(28).with(LocalTime.of(23, 0)),
                        sonstiges,
                        loc.get("Südpol")));

        events.add(
                event(
                        "Neon Nights – 80s & 90s Party",
                        "Synthwave, Eurodance und die grössten Hits der 80er und 90er. Dress"
                                + " code: Neon. Glow sticks inklusive.",
                        "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?w=800&h=500&fit=crop",
                        now.plusDays(33).with(LocalTime.of(22, 0)),
                        now.plusDays(34).with(LocalTime.of(4, 0)),
                        sonstiges,
                        loc.get("Komplex 457")));

        events.add(
                event(
                        "Sunday Sunset Sessions",
                        "Chill-House und Downtempo zum Sonnenuntergang. Entspannter Ausklang"
                                + " des Wochenendes an der Limmat.",
                        "https://images.unsplash.com/photo-1517457373958-b7bdd4587205?w=800&h=500&fit=crop",
                        now.plusDays(7).with(LocalTime.of(16, 0)),
                        now.plusDays(7).with(LocalTime.of(22, 0)),
                        sonstiges,
                        loc.get("Bar am Wasser")));

        events.add(
                event(
                        "Drum & Bass Therapy",
                        "Die monatliche DnB-Session im Hive. Liquid, Neurofunk und Jump-Up"
                                + " auf dem besten Soundsystem der Stadt.",
                        "https://images.unsplash.com/photo-1564585222527-c2777e5bc11e?w=800&h=500&fit=crop",
                        now.plusDays(38).with(LocalTime.of(23, 0)),
                        now.plusDays(39).with(LocalTime.of(6, 0)),
                        sonstiges,
                        loc.get("Hive Club")));

        return eventRepository.saveAll(events);
    }

    private Event event(
            String name,
            String description,
            String imageUrl,
            LocalDateTime start,
            LocalDateTime end,
            Category category,
            Location location) {
        return Event.builder()
                .name(name)
                .description(description)
                .imageUrl(imageUrl)
                .startDate(start)
                .endDate(end)
                .category(category)
                .location(location)
                .source("SEED")
                .build();
    }
}
