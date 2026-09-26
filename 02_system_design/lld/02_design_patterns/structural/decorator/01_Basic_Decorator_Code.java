/**
 * ============================================================
 *  BASIC DECORATOR PATTERN — THE COFFEE SHOP ANALOGY
 * ============================================================
 *
 * The simplest, cleanest implementation. 
 * Understand this FIRST before looking at anything advanced.
 *
 * The trick: A Decorator IS-A Coffee AND HAS-A Coffee.
 * This lets decorators wrap other decorators infinitely.
 */

// ----------------------------------------------------
// 1. The Component Interface (The Menu Item Contract)
// ----------------------------------------------------
/**
 * Both real coffees AND decorators implement this.
 * This is what makes the wrapping possible — they share the same type.
 */
interface Coffee {
    double getCost();
    String getDescription();
}

// ----------------------------------------------------
// 2. Concrete Components (The Base Coffees)
// ----------------------------------------------------
/**
 * These are the actual, real coffees you start with.
 * They don't know decorators exist. They just do their job.
 */
class PlainCoffee implements Coffee {
    @Override
    public double getCost() {
        return 100.0;
    }

    @Override
    public String getDescription() {
        return "Plain Coffee";
    }
}

class Espresso implements Coffee {
    @Override
    public double getCost() {
        return 150.0;
    }

    @Override
    public String getDescription() {
        return "Espresso";
    }
}

// ----------------------------------------------------
// 3. The Abstract Decorator (The "Wrapper" Base Class)
// ----------------------------------------------------
/**
 * THIS IS THE HEART OF THE PATTERN.
 *
 * Notice two things:
 *   1. It IMPLEMENTS Coffee      (IS-A Coffee   → can be used anywhere a Coffee is expected)
 *   2. It HAS a Coffee field     (HAS-A Coffee  → wraps around an existing coffee)
 *
 * By default, it just delegates to the wrapped coffee.
 * Subclasses override to ADD their own behavior on top.
 */
abstract class CoffeeDecorator implements Coffee {
    
    protected Coffee wrappedCoffee;  // the thing we're decorating

    public CoffeeDecorator(Coffee coffee) {
        this.wrappedCoffee = coffee;
    }

    @Override
    public double getCost() {
        return wrappedCoffee.getCost();  // delegate to the inner coffee
    }

    @Override
    public String getDescription() {
        return wrappedCoffee.getDescription();  // delegate to the inner coffee
    }
}

// ----------------------------------------------------
// 4. Concrete Decorators (The Add-Ons)
// ----------------------------------------------------
/**
 * Each decorator adds its own cost and description ON TOP of whatever
 * it wraps. It doesn't care if it's wrapping a PlainCoffee or another Decorator.
 */
class MilkDecorator extends CoffeeDecorator {

    public MilkDecorator(Coffee coffee) {
        super(coffee);  // pass the coffee to be wrapped
    }

    @Override
    public double getCost() {
        return wrappedCoffee.getCost() + 20.0;  // add milk cost
    }

    @Override
    public String getDescription() {
        return wrappedCoffee.getDescription() + " + Milk";
    }
}

class WhippedCreamDecorator extends CoffeeDecorator {

    public WhippedCreamDecorator(Coffee coffee) {
        super(coffee);
    }

    @Override
    public double getCost() {
        return wrappedCoffee.getCost() + 30.0;
    }

    @Override
    public String getDescription() {
        return wrappedCoffee.getDescription() + " + Whipped Cream";
    }
}

class CaramelDecorator extends CoffeeDecorator {

    public CaramelDecorator(Coffee coffee) {
        super(coffee);
    }

    @Override
    public double getCost() {
        return wrappedCoffee.getCost() + 50.0;
    }

    @Override
    public String getDescription() {
        return wrappedCoffee.getDescription() + " + Caramel";
    }
}

// ----------------------------------------------------
// 5. Let's Run It!
// ----------------------------------------------------
public class BasicDecoratorDemo {
    public static void main(String[] args) {

        // === Order 1: Just a plain coffee ===
        Coffee order1 = new PlainCoffee();
        System.out.println(order1.getDescription() + " → ₹" + order1.getCost());
        // Output: Plain Coffee → ₹100.0

        // === Order 2: Espresso with Milk ===
        // Read inside-out: Start with Espresso, wrap with Milk
        Coffee order2 = new MilkDecorator(new Espresso());
        System.out.println(order2.getDescription() + " → ₹" + order2.getCost());
        // Output: Espresso + Milk → ₹170.0

        // === Order 3: Plain Coffee + Milk + Whipped Cream + Caramel ===
        // Each decorator wraps the previous one like Russian nesting dolls
        Coffee order3 = new CaramelDecorator(
                            new WhippedCreamDecorator(
                                new MilkDecorator(
                                    new PlainCoffee()
                                )
                            )
                         );
        System.out.println(order3.getDescription() + " → ₹" + order3.getCost());
        // Output: Plain Coffee + Milk + Whipped Cream + Caramel → ₹200.0

        // === Order 4: DOUBLE Milk! (decorators can be applied multiple times) ===
        Coffee order4 = new MilkDecorator(new MilkDecorator(new Espresso()));
        System.out.println(order4.getDescription() + " → ₹" + order4.getCost());
        // Output: Espresso + Milk + Milk → ₹190.0
    }
}

/**
 * 🧠 WHAT JUST HAPPENED? (Trace the call for Order 3)
 *
 * When we call order3.getCost():
 *
 *   CaramelDecorator.getCost()
 *     → wrappedCoffee.getCost() + 50          // wrappedCoffee = WhippedCreamDecorator
 *       → WhippedCreamDecorator.getCost()
 *         → wrappedCoffee.getCost() + 30      // wrappedCoffee = MilkDecorator
 *           → MilkDecorator.getCost()
 *             → wrappedCoffee.getCost() + 20  // wrappedCoffee = PlainCoffee
 *               → PlainCoffee.getCost()
 *                 → returns 100
 *               returns 100 + 20 = 120
 *             returns 120 + 30 = 150
 *           returns 150 + 50 = 200 ✓
 *
 * It's a chain of delegation. Each layer adds its own piece and passes the rest inward.
 *
 *
 * 💡 INTERVIEW TAKEAWAY:
 * If you are ever asked "Design a notification system" and they have multiple
 * notification channels (Email, SMS, Slack, Push), Decorator is a perfect fit:
 *
 *   Notifier base = new EmailNotifier();
 *   Notifier decorated = new SlackDecorator(new SMSDecorator(base));
 *   decorated.send("Server is down!");
 *   // Sends Email, then SMS, then Slack — all through decoration!
 */
