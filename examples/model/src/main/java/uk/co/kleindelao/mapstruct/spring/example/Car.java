package uk.co.kleindelao.mapstruct.spring.example;

public class Car {
    private String make;
    private SeatConfiguration seatConfiguration;
    private CarType type;

    public String getMake() {
        return make;
    }

    public void setMake(final String make) {
        this.make = make;
    }

    public SeatConfiguration getSeatConfiguration() {
        return seatConfiguration;
    }

    public void setSeatConfiguration(final SeatConfiguration seatConfiguration) {
        this.seatConfiguration = seatConfiguration;
    }

    public CarType getType() {
        return type;
    }

    public void setType(final CarType type) {
        this.type = type;
    }
}
