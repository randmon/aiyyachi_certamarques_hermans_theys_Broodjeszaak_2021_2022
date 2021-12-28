package model;

import model.database.BelegDB;
import model.database.BroodjesDB;
import model.domain.Beleg;
import model.domain.Broodje;
import model.domain.DomainException;
import model.domain.Item;
import model.domain.bestelling.Bestelling;
import model.domain.bestelling.BestellingEvent;
import model.domain.korting.Korting;
import model.domain.korting.KortingEnum;
import model.domain.korting.KortingFactory;

import java.util.*;

public class BestelFacade extends Observable {
    private BroodjesDB broodjesDB;
    private BelegDB belegDB;
    private Bestelling bestelling, inKitchen;
    private Deque<Bestelling> queue = new LinkedList<>();
    private int nextOrderID;

    public BestelFacade(String fileType) {
        broodjesDB = BroodjesDB.getInstance(fileType);
        belegDB = BelegDB.getInstance(fileType);
        bestelling = new Bestelling(1);
        nextOrderID = 2;
    }

    public Map<String, Broodje> getBroodjes() {
        return broodjesDB.getAll();
    }

    public Map<String, Beleg> getBeleg() {
        return belegDB.getAll();
    }

    public void saveVoorraad() {
        broodjesDB.save();
        belegDB.save();
    }

    public void startNewOrder() {
        bestelling.startOrder();
    }

    public int getOrderID() {
        return bestelling.getId();
    }

    public void addBroodje(Broodje b) {
        bestelling.addBroodje(b);
        broodjesDB.setVoorraad(b, b.getVoorraad() - 1);
        setChanged();
        notifyObservers(BestellingEvent.ADD_BROODJE);
    }

    public Bestelling getBestelling() {
        return bestelling;
    }

    public void addBeleg(Item item, Beleg beleg) {
        if (item == null) return;
        bestelling.addBeleg(item, beleg);
        belegDB.setVoorraad(beleg, beleg.getVoorraad() - 1);
        setChanged();
        notifyObservers(BestellingEvent.ADD_BELEG);
    }


    public void addSameItem(Item item) {
        if (item == null) {
            throw new DomainException("Item mag niet leeg zijn");
        }
        bestelling.addSameBroodje(item.getBroodje(), item.getBeleg());
        broodjesDB.setVoorraad(item.getBroodje(), item.getBroodje().getVoorraad() - 1);
        for (Beleg b : item.getBeleg()) belegDB.setVoorraad(b, b.getVoorraad() - 1);

        setChanged();
        notifyObservers(BestellingEvent.ADD_SAME_BROODJE);
    }

    public void deleteItem(Item item) {
        if (item == null) {
            throw new DomainException("Item mag niet leeg zijn");
        }
        bestelling.removeBroodje(item);
        broodjesDB.setVoorraad(item.getBroodje(), item.getBroodje().getVoorraad() + 1);
        for (Beleg b : item.getBeleg()) belegDB.setVoorraad(b, b.getVoorraad() + 1);
        setChanged();
        notifyObservers(BestellingEvent.REMOVE_BROODJE);
    }

    public void cancelOrder() {
        bestelling.cancelOrder();

        //Put voorraad back
        Iterator<Item> i = bestelling.getItems().iterator();

        while (i.hasNext()) {
            Item item = i.next();

            i.remove();
            broodjesDB.setVoorraad(item.getBroodje(), item.getBroodje().getVoorraad() + 1);
            for (Beleg b : item.getBeleg()) belegDB.setVoorraad(b, b.getVoorraad() + 1);
        }

        bestelling = new Bestelling(nextOrderID);
        nextOrderID++;

        setChanged();
        notifyObservers(BestellingEvent.CANCEL_ORDER);
    }

    public double closeOrder(String korting) {
        Korting k = KortingFactory.getKorting(korting);
        bestelling.closeOrder();
        return k.berekenKorting(bestelling);
    }

    public void pay() {
        bestelling.pay();
    }

    public void toKitchen() {
        bestelling.sendToKitchen();

        //Change aantal besteld
        for (Item item : bestelling.getItems()) {
            item.getBroodje().setBesteld(item.getBroodje().getBesteld() + 1);
            for (Beleg b : item.getBeleg()) b.setBesteld(b.getBesteld() + 1);
        }

        queue.add(bestelling);
        bestelling = new Bestelling(nextOrderID);
        nextOrderID++;

        setChanged();
        notifyObservers(BestellingEvent.SEND_TO_KITCHEN);
    }

    public int getInWachtrij() {
        return queue.size();
    }

    public List<String> getKortingLijst() {
        List<String> kortingLijst = new ArrayList<>();
        for (KortingEnum e : KortingEnum.values()) kortingLijst.add(e.getText());
        return kortingLijst;
    }

    public Bestelling getNextInRij() {
        try {
            inKitchen = queue.removeFirst();
            inKitchen.startBereiding();
            return inKitchen;
        } catch (NoSuchElementException e) {
            throw new DomainException("Er zijn geen bestellingen in de wachtrij");
        }
    }

    public List<String> getItemsForKitchen() {
        return inKitchen.getItemsForKitchen();
    }
}
