package com.example.MCPTravel.config;

import com.example.MCPTravel.entity.*;
import com.example.MCPTravel.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final MenuItemRepository menuItemRepository;
    private final ReportRepository reportRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String DEFAULT_PASSWORD = "AiraloAutomation1!";

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already initialized, skipping...");
            return;
        }

        log.info("Initializing database with sample data...");

        // Create Users
        List<User> users = createUsers();

        // Create Companies
        List<Company> companies = createCompanies(users);

        // Create Menu Items
        createMenuItems(companies);

        // Create Reports
        createReports(users, companies);

        log.info("Database initialization completed!");
        log.info("Created {} users, {} companies", users.size(), companies.size());
    }

    private List<User> createUsers() {
        String encodedPassword = passwordEncoder.encode(DEFAULT_PASSWORD);

        List<User> users = List.of(
            // Admins
            User.builder().username("admin").email("admin@mcptravel.com").password(encodedPassword).role(Role.ADMIN).build(),
            User.builder().username("superadmin").email("superadmin@mcptravel.com").password(encodedPassword).role(Role.ADMIN).build(),

            // Business Owners
            User.builder().username("owner_john").email("john@restaurants.com").password(encodedPassword).role(Role.BUSINESS_OWNER).build(),
            User.builder().username("owner_maria").email("maria@cafes.com").password(encodedPassword).role(Role.BUSINESS_OWNER).build(),
            User.builder().username("owner_alex").email("alex@bars.com").password(encodedPassword).role(Role.BUSINESS_OWNER).build(),
            User.builder().username("owner_sofia").email("sofia@hotels.com").password(encodedPassword).role(Role.BUSINESS_OWNER).build(),
            User.builder().username("owner_david").email("david@shops.com").password(encodedPassword).role(Role.BUSINESS_OWNER).build(),
            User.builder().username("owner_elena").email("elena@spas.com").password(encodedPassword).role(Role.BUSINESS_OWNER).build(),
            User.builder().username("owner_mike").email("mike@gyms.com").password(encodedPassword).role(Role.BUSINESS_OWNER).build(),
            User.builder().username("owner_anna").email("anna@bakeries.com").password(encodedPassword).role(Role.BUSINESS_OWNER).build(),

            // Regular Users
            User.builder().username("user_victor").email("victor@gmail.com").password(encodedPassword).role(Role.USER).build(),
            User.builder().username("user_diana").email("diana@gmail.com").password(encodedPassword).role(Role.USER).build(),
            User.builder().username("user_andrei").email("andrei@yahoo.com").password(encodedPassword).role(Role.USER).build(),
            User.builder().username("user_cristina").email("cristina@outlook.com").password(encodedPassword).role(Role.USER).build(),
            User.builder().username("user_sergiu").email("sergiu@mail.com").password(encodedPassword).role(Role.USER).build(),
            User.builder().username("user_natalia").email("natalia@gmail.com").password(encodedPassword).role(Role.USER).build(),
            User.builder().username("user_ion").email("ion@yahoo.com").password(encodedPassword).role(Role.USER).build(),
            User.builder().username("user_olga").email("olga@outlook.com").password(encodedPassword).role(Role.USER).build()
        );

        return userRepository.saveAll(users);
    }

    private List<Company> createCompanies(List<User> users) {
        User owner1 = users.stream().filter(u -> u.getUsername().equals("owner_john")).findFirst().orElseThrow();
        User owner2 = users.stream().filter(u -> u.getUsername().equals("owner_maria")).findFirst().orElseThrow();
        User owner3 = users.stream().filter(u -> u.getUsername().equals("owner_alex")).findFirst().orElseThrow();
        User owner4 = users.stream().filter(u -> u.getUsername().equals("owner_sofia")).findFirst().orElseThrow();
        User owner5 = users.stream().filter(u -> u.getUsername().equals("owner_david")).findFirst().orElseThrow();
        User owner6 = users.stream().filter(u -> u.getUsername().equals("owner_elena")).findFirst().orElseThrow();
        User owner7 = users.stream().filter(u -> u.getUsername().equals("owner_mike")).findFirst().orElseThrow();
        User owner8 = users.stream().filter(u -> u.getUsername().equals("owner_anna")).findFirst().orElseThrow();

        List<Company> companies = List.of(
            // Restaurants (owner1 - John)
            createCompany("La Placinte", "Traditional Moldovan restaurant with authentic cuisine", "Str. Stefan cel Mare 45, Chisinau", 47.0245, 28.8325, "+373 22 123 456", "https://laplacinte.md", "restaurant", workingHours("08:00", "23:00"), CompanyStatus.OPEN, owner1),
            createCompany("Symposium Cafe", "Fine dining restaurant with European cuisine", "Bd. Dacia 12, Chisinau", 47.0156, 28.8412, "+373 22 234 567", "https://symposium.md", "restaurant", workingHours("11:00", "23:00"), CompanyStatus.OPEN, owner1),
            createCompany("Propaganda Pizza", "Best pizza in town with craft beer", "Str. Alexandru cel Bun 89, Chisinau", 47.0289, 28.8156, "+373 22 345 678", "https://propaganda.md", "restaurant", workingHours("10:00", "02:00"), CompanyStatus.OPEN, owner1),
            createCompany("Grill House", "Premium steakhouse", "Str. Puskin 22, Chisinau", 47.0198, 28.8289, "+373 22 456 789", "https://grillhouse.md", "restaurant", workingHours("12:00", "23:00"), CompanyStatus.BUSY, owner1),
            createCompany("Sushi Master", "Japanese cuisine and sushi bar", "Str. Bucuresti 67, Chisinau", 47.0312, 28.8198, "+373 22 567 890", "https://sushimaster.md", "restaurant", workingHours("11:00", "22:00"), CompanyStatus.OPEN, owner1),

            // Cafes (owner2 - Maria)
            createCompany("Tucano Coffee", "Specialty coffee and desserts", "Str. Columna 142, Chisinau", 47.0267, 28.8378, "+373 22 111 222", "https://tucano.md", "cafe", workingHours("07:00", "22:00"), CompanyStatus.OPEN, owner2),
            createCompany("Artcafe", "Cozy cafe with art exhibitions", "Str. Mitropolit Varlaam 77, Chisinau", 47.0234, 28.8267, "+373 22 222 333", "https://artcafe.md", "cafe", workingHours("08:00", "21:00"), CompanyStatus.OPEN, owner2),
            createCompany("Marukame Coffee", "Japanese style coffee shop", "Str. Armeneasca 56, Chisinau", 47.0178, 28.8345, "+373 22 333 444", "https://marukame.md", "cafe", workingHours("08:00", "20:00"), CompanyStatus.OPEN, owner2),
            createCompany("Central Coffee", "Classic coffeehouse downtown", "Bd. Stefan cel Mare 134, Chisinau", 47.0223, 28.8301, "+373 22 444 555", "https://centralcoffee.md", "cafe", workingHours("07:00", "23:00"), CompanyStatus.OPEN, owner2),
            createCompany("Beans & Leaves", "Organic coffee and tea house", "Str. Ismail 88, Chisinau", 47.0145, 28.8234, "+373 22 555 666", "https://beansleaves.md", "cafe", workingHours("09:00", "19:00"), CompanyStatus.TEMPORARILY_CLOSED, owner2),

            // Bars (owner3 - Alex)
            createCompany("Black Elephant", "Craft beer bar with live music", "Str. 31 August 112, Chisinau", 47.0256, 28.8156, "+373 22 666 777", "https://blackelephant.md", "bar", workingHours("18:00", "03:00"), CompanyStatus.OPEN, owner3),
            createCompany("Mojo Lounge", "Cocktail bar and club", "Str. Banulescu Bodoni 45, Chisinau", 47.0189, 28.8412, "+373 22 777 888", "https://mojo.md", "bar", workingHours("20:00", "05:00"), CompanyStatus.OPEN, owner3),
            createCompany("Decanter Wine Bar", "Premium wine selection", "Str. Mihai Eminescu 34, Chisinau", 47.0267, 28.8323, "+373 22 888 999", "https://decanter.md", "bar", workingHours("16:00", "00:00"), CompanyStatus.OPEN, owner3),
            createCompany("Irish Pub", "Traditional Irish atmosphere", "Str. Alexandru cel Bun 67, Chisinau", 47.0234, 28.8178, "+373 22 999 000", "https://irishpub.md", "bar", workingHours("12:00", "02:00"), CompanyStatus.BUSY, owner3),

            // Hotels (owner4 - Sofia)
            createCompany("Radisson Blu Leogrand", "5-star luxury hotel", "Str. Mitropolit Varlaam 77, Chisinau", 47.0245, 28.8267, "+373 22 100 200", "https://radissonblu.md", "hotel", workingHours("00:00", "23:59"), CompanyStatus.OPEN, owner4),
            createCompany("Nobil Luxury Boutique", "Boutique hotel in city center", "Str. Sfatul Tarii 19, Chisinau", 47.0212, 28.8356, "+373 22 200 300", "https://nobil.md", "hotel", workingHours("00:00", "23:59"), CompanyStatus.OPEN, owner4),
            createCompany("Hotel Codru", "Business class hotel", "Str. 31 August 127, Chisinau", 47.0278, 28.8134, "+373 22 300 400", "https://codru.md", "hotel", workingHours("00:00", "23:59"), CompanyStatus.OPEN, owner4),
            createCompany("City Park Hotel", "Modern hotel near Central Park", "Str. Valea Trandafirilor 6, Chisinau", 47.0156, 28.8412, "+373 22 400 500", "https://citypark.md", "hotel", workingHours("00:00", "23:59"), CompanyStatus.TEMPORARILY_CLOSED, owner4),

            // Shops (owner5 - David)
            createCompany("Nr.1 Mall", "Largest shopping center in Moldova", "Bd. Stefan cel Mare 8, Chisinau", 47.0234, 28.8289, "+373 22 500 600", "https://nr1.md", "shop", workingHours("10:00", "22:00"), CompanyStatus.OPEN, owner5),
            createCompany("MallDova", "Premium shopping destination", "Str. Arborilor 21, Chisinau", 47.0312, 28.8156, "+373 22 600 700", "https://malldova.md", "shop", workingHours("10:00", "22:00"), CompanyStatus.OPEN, owner5),
            createCompany("Sun City", "Entertainment and shopping", "Str. Dacia 24, Chisinau", 47.0145, 28.8378, "+373 22 700 800", "https://suncity.md", "shop", workingHours("09:00", "21:00"), CompanyStatus.OPEN, owner5),
            createCompany("Gemeni Shop", "Electronics and gadgets", "Str. Ismail 106, Chisinau", 47.0189, 28.8234, "+373 22 800 900", "https://gemeni.md", "shop", workingHours("10:00", "20:00"), CompanyStatus.OPEN, owner5),

            // Spas (owner6 - Elena)
            createCompany("Spa Venecia", "Luxury spa and wellness center", "Str. Calea Iesilor 8, Chisinau", 47.0267, 28.8412, "+373 22 111 333", "https://venecia.md", "spa", workingHours("09:00", "21:00"), CompanyStatus.OPEN, owner6),
            createCompany("Aqua Day Spa", "Premium spa treatments", "Str. Petru Rares 45, Chisinau", 47.0198, 28.8178, "+373 22 222 444", "https://aquadayspa.md", "spa", workingHours("10:00", "20:00"), CompanyStatus.OPEN, owner6),
            createCompany("Relax Zone", "Massage and relaxation", "Str. Ion Creanga 78, Chisinau", 47.0289, 28.8301, "+373 22 333 555", "https://relaxzone.md", "spa", workingHours("11:00", "19:00"), CompanyStatus.OPEN, owner6),

            // Gyms (owner7 - Mike)
            createCompany("World Class", "Premium fitness club", "Str. Alexandru cel Bun 45, Chisinau", 47.0234, 28.8356, "+373 22 444 666", "https://worldclass.md", "gym", workingHours("06:00", "23:00"), CompanyStatus.OPEN, owner7),
            createCompany("Flex Gym", "24/7 fitness center", "Str. Columna 89, Chisinau", 47.0156, 28.8267, "+373 22 555 777", "https://flexgym.md", "gym", workingHours("00:00", "23:59"), CompanyStatus.OPEN, owner7),
            createCompany("Sport Life", "Family fitness club", "Bd. Moscova 12, Chisinau", 47.0312, 28.8134, "+373 22 666 888", "https://sportlife.md", "gym", workingHours("07:00", "22:00"), CompanyStatus.BUSY, owner7),
            createCompany("CrossFit Chisinau", "CrossFit training center", "Str. Industriala 34, Chisinau", 47.0178, 28.8412, "+373 22 777 999", "https://crossfit.md", "gym", workingHours("06:00", "21:00"), CompanyStatus.OPEN, owner7),

            // Bakeries (owner8 - Anna)
            createCompany("Panilino", "Fresh bread and pastries", "Str. Stefan cel Mare 156, Chisinau", 47.0245, 28.8289, "+373 22 888 111", "https://panilino.md", "bakery", workingHours("07:00", "20:00"), CompanyStatus.OPEN, owner8),
            createCompany("Franzeluta", "Traditional Moldovan bakery", "Str. Vasile Alecsandri 22, Chisinau", 47.0212, 28.8178, "+373 22 999 222", "https://franzeluta.md", "bakery", workingHours("06:00", "21:00"), CompanyStatus.OPEN, owner8),
            createCompany("Sweet Dreams", "Cakes and custom orders", "Str. Tighina 49, Chisinau", 47.0267, 28.8345, "+373 22 000 333", "https://sweetdreams.md", "bakery", workingHours("08:00", "19:00"), CompanyStatus.OPEN, owner8),
            createCompany("Croissant House", "French pastries", "Str. Armeneasca 67, Chisinau", 47.0189, 28.8234, "+373 22 123 321", "https://croissant.md", "bakery", workingHours("07:30", "18:30"), CompanyStatus.OPEN, owner8)
        );

        return companyRepository.saveAll(companies);
    }

    private Company createCompany(String name, String description, String address, Double lat, Double lng, String phone, String website, String category, Map<String, String> hours, CompanyStatus status, User owner) {
        return Company.builder()
            .name(name)
            .description(description)
            .address(address)
            .latitude(lat)
            .longitude(lng)
            .phoneNumber(phone)
            .website(website)
            .category(category)
            .workingHours(hours)
            .status(status)
            .owner(owner)
            .build();
    }

    private Map<String, String> workingHours(String open, String close) {
        Map<String, String> hours = new HashMap<>();
        String range = open + "-" + close;
        hours.put("MONDAY", range);
        hours.put("TUESDAY", range);
        hours.put("WEDNESDAY", range);
        hours.put("THURSDAY", range);
        hours.put("FRIDAY", range);
        hours.put("SATURDAY", range);
        hours.put("SUNDAY", range);
        return hours;
    }

    private void createMenuItems(List<Company> companies) {
        for (Company company : companies) {
            switch (company.getCategory()) {
                case "restaurant" -> createRestaurantMenu(company);
                case "cafe" -> createCafeMenu(company);
                case "bar" -> createBarMenu(company);
                case "bakery" -> createBakeryMenu(company);
            }
        }
    }

    private void createRestaurantMenu(Company company) {
        List<MenuItem> items = List.of(
            menuItem("Caesar Salad", "Fresh romaine lettuce with parmesan and croutons", "45.00", "salads", company),
            menuItem("Greek Salad", "Tomatoes, cucumbers, olives, feta cheese", "40.00", "salads", company),
            menuItem("Grilled Salmon", "Atlantic salmon with vegetables", "185.00", "main", company),
            menuItem("Beef Steak", "Premium beef with mashed potatoes", "220.00", "main", company),
            menuItem("Chicken Pasta", "Penne with grilled chicken and cream sauce", "95.00", "main", company),
            menuItem("Mushroom Risotto", "Arborio rice with porcini mushrooms", "85.00", "main", company),
            menuItem("Margherita Pizza", "Tomato sauce, mozzarella, basil", "75.00", "pizza", company),
            menuItem("Pepperoni Pizza", "Tomato sauce, mozzarella, pepperoni", "95.00", "pizza", company),
            menuItem("Tiramisu", "Classic Italian dessert", "55.00", "desserts", company),
            menuItem("Cheesecake", "New York style cheesecake", "50.00", "desserts", company),
            menuItem("Fresh Orange Juice", "Freshly squeezed", "35.00", "drinks", company),
            menuItem("Lemonade", "Homemade lemonade with mint", "30.00", "drinks", company)
        );
        menuItemRepository.saveAll(items);
    }

    private void createCafeMenu(Company company) {
        List<MenuItem> items = List.of(
            menuItem("Espresso", "Strong Italian coffee", "25.00", "coffee", company),
            menuItem("Americano", "Espresso with hot water", "30.00", "coffee", company),
            menuItem("Cappuccino", "Espresso with steamed milk foam", "35.00", "coffee", company),
            menuItem("Latte", "Espresso with steamed milk", "40.00", "coffee", company),
            menuItem("Flat White", "Double espresso with microfoam", "45.00", "coffee", company),
            menuItem("Mocha", "Espresso with chocolate and milk", "50.00", "coffee", company),
            menuItem("Green Tea", "Japanese sencha", "25.00", "tea", company),
            menuItem("Earl Grey", "Classic black tea with bergamot", "25.00", "tea", company),
            menuItem("Croissant", "Butter croissant", "25.00", "pastry", company),
            menuItem("Chocolate Muffin", "Rich chocolate muffin", "30.00", "pastry", company),
            menuItem("Avocado Toast", "Sourdough with avocado and eggs", "65.00", "food", company),
            menuItem("Club Sandwich", "Triple decker sandwich", "75.00", "food", company)
        );
        menuItemRepository.saveAll(items);
    }

    private void createBarMenu(Company company) {
        List<MenuItem> items = List.of(
            menuItem("Craft IPA", "Local craft India Pale Ale, 0.5L", "55.00", "beer", company),
            menuItem("Pilsner", "Czech style lager, 0.5L", "45.00", "beer", company),
            menuItem("Stout", "Dark Irish stout, 0.5L", "60.00", "beer", company),
            menuItem("Mojito", "Rum, mint, lime, soda", "85.00", "cocktails", company),
            menuItem("Margarita", "Tequila, lime, triple sec", "90.00", "cocktails", company),
            menuItem("Old Fashioned", "Bourbon, bitters, sugar", "95.00", "cocktails", company),
            menuItem("Whiskey Sour", "Bourbon, lemon, egg white", "85.00", "cocktails", company),
            menuItem("Aperol Spritz", "Aperol, prosecco, soda", "80.00", "cocktails", company),
            menuItem("Red Wine Glass", "House red wine", "50.00", "wine", company),
            menuItem("White Wine Glass", "House white wine", "50.00", "wine", company),
            menuItem("Nachos", "Tortilla chips with cheese and salsa", "65.00", "snacks", company),
            menuItem("Chicken Wings", "Spicy buffalo wings", "75.00", "snacks", company)
        );
        menuItemRepository.saveAll(items);
    }

    private void createBakeryMenu(Company company) {
        List<MenuItem> items = List.of(
            menuItem("White Bread", "Classic white loaf", "15.00", "bread", company),
            menuItem("Whole Wheat Bread", "Healthy whole grain bread", "18.00", "bread", company),
            menuItem("Sourdough", "Traditional sourdough loaf", "25.00", "bread", company),
            menuItem("Baguette", "French style baguette", "12.00", "bread", company),
            menuItem("Butter Croissant", "Flaky French croissant", "20.00", "pastry", company),
            menuItem("Pain au Chocolat", "Chocolate filled pastry", "25.00", "pastry", company),
            menuItem("Cinnamon Roll", "Sweet cinnamon pastry", "22.00", "pastry", company),
            menuItem("Apple Strudel", "Traditional apple strudel", "30.00", "pastry", company),
            menuItem("Birthday Cake", "Custom birthday cake per kg", "350.00", "cakes", company),
            menuItem("Chocolate Cake Slice", "Rich chocolate cake", "45.00", "cakes", company),
            menuItem("Fruit Tart", "Fresh seasonal fruits", "55.00", "cakes", company),
            menuItem("Eclair", "Chocolate glazed eclair", "28.00", "pastry", company)
        );
        menuItemRepository.saveAll(items);
    }

    private MenuItem menuItem(String name, String description, String price, String category, Company company) {
        return MenuItem.builder()
            .name(name)
            .description(description)
            .price(new BigDecimal(price))
            .category(category)
            .isAvailable(true)
            .company(company)
            .build();
    }

    private void createReports(List<User> users, List<Company> companies) {
        User user1 = users.stream().filter(u -> u.getUsername().equals("user_victor")).findFirst().orElseThrow();
        User user2 = users.stream().filter(u -> u.getUsername().equals("user_diana")).findFirst().orElseThrow();
        User user3 = users.stream().filter(u -> u.getUsername().equals("user_andrei")).findFirst().orElseThrow();
        User admin = users.stream().filter(u -> u.getUsername().equals("admin")).findFirst().orElseThrow();

        Company company1 = companies.get(0);
        Company company2 = companies.get(5);
        Company company3 = companies.get(10);

        List<Report> reports = List.of(
            Report.builder().description("Wrong working hours - The restaurant was closed at 21:00 but website says 23:00").reporter(user1).company(company1).status(ReportStatus.PENDING).build(),
            Report.builder().description("Menu prices outdated - prices on the website are different from actual prices").reporter(user2).company(company1).status(ReportStatus.APPROVED).reviewedBy(admin).adminNotes("Contacted owner, prices updated").build(),
            Report.builder().description("Waiter was very rude and unprofessional").reporter(user1).company(company2).status(ReportStatus.PENDING).build(),
            Report.builder().description("Food quality issue - Found a hair in my salad").reporter(user3).company(company2).status(ReportStatus.APPROVED).reviewedBy(admin).adminNotes("Warning issued to establishment").build(),
            Report.builder().description("Google maps location is wrong, hard to find the place").reporter(user2).company(company3).status(ReportStatus.DENIED).reviewedBy(admin).adminNotes("Location verified as correct").build(),
            Report.builder().description("No wheelchair access - Website says accessible but no ramp available").reporter(user3).company(company1).status(ReportStatus.PENDING).build()
        );

        reportRepository.saveAll(reports);
    }
}
