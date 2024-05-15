import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class TicketSeller {
    private int ticketsAvailable;
    private final Lock lock = new ReentrantLock();
    private final Condition ticketsAvailableCondition = lock.newCondition();

    public TicketSeller(int initialTickets) {
        this.ticketsAvailable = initialTickets;
    }

    public void sellTickets(int amount) {
        lock.lock();
        try {
            while (ticketsAvailable < amount) {
                System.out.println(Thread.currentThread().getName() + " muốn mua " + amount + " vé nhưng chỉ còn " + ticketsAvailable + " vé. Đợi...");
                ticketsAvailableCondition.await();
            }
            ticketsAvailable -= amount;
            System.out.println(Thread.currentThread().getName() + " đã mua " + amount + " vé. Số vé còn lại: " + ticketsAvailable);
            ticketsAvailableCondition.signalAll(); // Báo hiệu cho các thread khác
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }

    public int getTicketsAvailable() {
        lock.lock();
        try {
            return ticketsAvailable;
        } finally {
            lock.unlock();
        }
    }
}

class Customer implements Runnable {
    private TicketSeller ticketSeller;
    private int ticketsToBuy;

    public Customer(TicketSeller ticketSeller, int ticketsToBuy) {
        this.ticketSeller = ticketSeller;
        this.ticketsToBuy = ticketsToBuy;
    }

    @Override
    public void run() {
        ticketSeller.sellTickets(ticketsToBuy);
    }
}

public class TicketSellingSystem {
    public static void main(String[] args) {
        int initialTickets = 10;
        TicketSeller ticketSeller = new TicketSeller(initialTickets);

        // Tạo một số khách hàng, mỗi khách hàng muốn mua số lượng vé khác nhau
        Thread customer1 = new Thread(new Customer(ticketSeller, 4), "Khách hàng 1");
        Thread customer2 = new Thread(new Customer(ticketSeller, 3), "Khách hàng 2");
        Thread customer3 = new Thread(new Customer(ticketSeller, 2), "Khách hàng 3");
        Thread customer4 = new Thread(new Customer(ticketSeller, 5), "Khách hàng 4");

        // Bắt đầu các thread khách hàng
        customer1.start();
        customer2.start();
        customer3.start();
        customer4.start();

        // Đảm bảo tất cả các khách hàng đã hoàn thành việc mua vé
        try {
            customer1.join();
            customer2.join();
            customer3.join();
            customer4.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Hiển thị số vé còn lại
        System.out.println("Số vé còn lại cuối cùng: " + ticketSeller.getTicketsAvailable());
    }
}
