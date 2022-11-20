package model;

public class OrderBookElement {

    private final Float price;
    private final Float volume;

    public OrderBookElement(Float price, Float volume) {
        this.price = price;
        this.volume = volume;
    }

    public float getPrice() {
        return price;
    }

    public float getVolume() {
        return volume;
    }
}