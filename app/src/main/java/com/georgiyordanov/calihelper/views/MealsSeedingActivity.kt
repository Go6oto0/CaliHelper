package com.georgiyordanov.calihelper.views

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.georgiyordanov.calihelper.R
import com.georgiyordanov.calihelper.data.models.Meal
import com.google.firebase.firestore.FirebaseFirestore

class MealsSeedingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_meals) // or any layout if needed

        val db = FirebaseFirestore.getInstance()
        val lightVeganMeals = listOf(
            Meal(
                id = "med_cucumber_chickpea_salad",
                name = "Mediterranean Cucumber Chickpea Salad",
                description = "A bright and refreshing salad with cucumber, tomato, red onion, mint, and chickpeas.",
                dietType = "vegan",
                density = "light",
                ingredients = listOf(
                    "chickpeas",
                    "cucumber",
                    "cherry tomatoes",
                    "red onion",
                    "fresh mint",
                    "olive oil",
                    "lemon juice",
                    "salt",
                    "pepper"
                ),
                instructions = listOf(
                    "Combine chickpeas, cucumber, tomatoes, onion, and mint in a bowl.",
                    "Whisk olive oil, lemon juice, salt, and pepper in a small bowl.",
                    "Pour dressing over salad and toss gently.",
                    "Let rest for 5–10 minutes before serving."
                ),
                imageUrl = null,
                calories = 300
            ),
            Meal(
                id = "chickpea_avocado_salad",
                name = "Chickpea Avocado Salad with Lemon Dressing",
                description = "A creamy yet light salad of mashed chickpeas and avocado in a zesty lemon dressing.",
                dietType = "vegan",
                density = "light",
                ingredients = listOf(
                    "chickpeas",
                    "avocado",
                    "lemon juice",
                    "olive oil",
                    "Dijon mustard",
                    "garlic powder",
                    "salt",
                    "pepper",
                    "parsley"
                ),
                instructions = listOf(
                    "Mash chickpeas and avocado together, leaving some texture.",
                    "Whisk lemon juice, olive oil, mustard, garlic powder, salt, and pepper.",
                    "Pour dressing over chickpea–avocado mixture and stir to combine.",
                    "Garnish with parsley and serve immediately."
                ),
                imageUrl = null,
                calories = 350
            ),
            Meal(
                id = "fresh_black_bean_burrito_bowl",
                name = "Fresh Black Bean Burrito Bowl",
                description = "A vibrant bowl of cilantro‑lime rice topped with black beans, pickled onions, and cilantro pesto.",
                dietType = "vegan",
                density = "light",
                ingredients = listOf(
                    "brown rice",
                    "lime",
                    "black beans",
                    "red onion",
                    "apple cider vinegar",
                    "sugar",
                    "cilantro",
                    "garlic",
                    "olive oil"
                ),
                instructions = listOf(
                    "Cook brown rice and toss with lime juice.",
                    "Quick‑pickle red onion by soaking in vinegar and sugar for 10 minutes.",
                    "Layer rice, black beans, and pickled onions in bowls.",
                    "Drizzle with cilantro pesto and garnish with lime wedges."
                ),
                imageUrl = null,
                calories = 400
            )
        )

        // Seed each meal into Firestore under the "meals" collection
        for (meal in lightVeganMeals) {
            db.collection("meals")
                .document(meal.id)
                .set(meal)
        }
        val mediumVeganMeals = listOf(
            Meal(
                id = "meal_buddha_quinoa_bowl",
                name = "Rainbow Buddha Quinoa Bowl",
                description = "A nourishing bowl of quinoa topped with roasted sweet potato, chickpeas, avocado, and tahini drizzle.",
                dietType = "vegan",
                density = "medium",
                ingredients = listOf(
                    "quinoa",
                    "sweet potato",
                    "chickpeas",
                    "avocado",
                    "spinach",
                    "tahini",
                    "lemon juice",
                    "olive oil",
                    "salt",
                    "pepper"
                ),
                instructions = listOf(
                    "Cook quinoa according to package instructions.",
                    "Roast diced sweet potato and chickpeas with olive oil, salt, and pepper at 200 °C for 25 minutes.",
                    "Assemble bowl: quinoa, roasted veggies, fresh spinach, sliced avocado.",
                    "Drizzle with a sauce of tahini whisked with lemon juice and a bit of water."
                ),
                imageUrl = "https://example.com/buddha_quinoa_bowl.jpg",
                calories = 550
            ),

            Meal(
                id = "meal_lentil_tomato_soup",
                name = "Hearty Lentil Tomato Soup",
                description = "A comforting medium‑density soup full of red lentils, tomatoes, and warming spices.",
                dietType = "vegan",
                density = "medium",
                ingredients = listOf(
                    "red lentils",
                    "canned tomatoes",
                    "onion",
                    "garlic",
                    "carrot",
                    "celery",
                    "vegetable broth",
                    "cumin",
                    "paprika",
                    "olive oil",
                    "salt",
                    "pepper"
                ),
                instructions = listOf(
                    "Sauté chopped onion, garlic, carrot, and celery in olive oil until soft.",
                    "Stir in cumin and paprika and cook 1 minute.",
                    "Add lentils, canned tomatoes, and vegetable broth; bring to a boil.",
                    "Simmer 20 minutes until lentils are tender; season with salt and pepper."
                ),
                imageUrl = "https://example.com/lentil_tomato_soup.jpg",
                calories = 480
            ),

            Meal(
                id = "meal_teriyaki_tofu_stirfry",
                name = "Teriyaki Tofu & Veggie Stir‑Fry",
                description = "A satisfying medium‑density stir‑fry with tofu, broccoli, bell peppers, and homemade teriyaki sauce.",
                dietType = "vegan",
                density = "medium",
                ingredients = listOf(
                    "firm tofu",
                    "broccoli florets",
                    "bell pepper",
                    "soy sauce",
                    "maple syrup",
                    "rice vinegar",
                    "garlic",
                    "ginger",
                    "cornstarch",
                    "sesame oil",
                    "green onions"
                ),
                instructions = listOf(
                    "Press and cube tofu; toss with cornstarch and pan‑fry until golden.",
                    "Stir‑fry garlic, ginger, broccoli, and bell pepper in sesame oil until crisp‑tender.",
                    "Whisk soy sauce, maple syrup, rice vinegar; add to pan with tofu and veggies.",
                    "Cook until sauce thickens; garnish with sliced green onions."
                ),
                imageUrl = "https://example.com/teriyaki_tofu_stirfry.jpg",
                calories = 620
            )
        )
        for (meal in mediumVeganMeals) {
            db.collection("meals").document(meal.id).set(meal)
        }
        val denseVeganMeals = listOf(
            Meal(
                id = "vegan_cashew_mac_and_cheese",
                name = "Vegan Cashew Mac and Cheese",
                description = "Ultra‑creamy comfort food made with a rich cashew cheese sauce.",
                dietType = "vegan",
                density = "dense",
                ingredients = listOf(
                    "elbow pasta",
                    "raw cashews (soaked)",
                    "nutritional yeast",
                    "garlic powder",
                    "turmeric",
                    "lemon juice",
                    "salt",
                    "black pepper"
                ),
                instructions = listOf(
                    "Soak cashews in water for at least 4 hours or boil for 10 minutes.",
                    "Drain cashews and blend with nutritional yeast, garlic powder, turmeric, lemon juice, salt, and pepper until smooth.",
                    "Cook pasta according to package directions; drain and return to pot.",
                    "Pour cashew sauce over pasta, stir to combine, and heat through before serving."
                ),
                imageUrl = null,
                calories = 470
            ),

            Meal(
                id = "vegan_lentil_shepherds_pie",
                name = "Vegan Lentil Shepherd’s Pie",
                description = "Hearty lentil filling topped with creamy mashed potatoes for a warming casserole.",
                dietType = "vegan",
                density = "dense",
                ingredients = listOf(
                    "red lentils",
                    "onion",
                    "carrots",
                    "celery",
                    "garlic",
                    "tomato paste",
                    "vegetable broth",
                    "thyme",
                    "rosemary",
                    "Russet potatoes",
                    "plant-based butter",
                    "plant-based milk"
                ),
                instructions = listOf(
                    "Sauté onion, carrots, celery, and garlic until softened.",
                    "Stir in lentils, tomato paste, broth, and herbs; simmer until lentils are tender.",
                    "Boil potatoes until soft; mash with butter and milk.",
                    "Layer lentil mixture in a baking dish, top with mashed potatoes, and bake at 200 °C until golden."
                ),
                imageUrl = null,
                calories = 569
            ),

            Meal(
                id = "decadent_vegan_lasagna",
                name = "Decadent Vegan Lasagna",
                description = "Layered vegan bolognese, ricotta, and cheese sauce baked to bubbly perfection.",
                dietType = "vegan",
                density = "dense",
                ingredients = listOf(
                    "lasagna noodles",
                    "cremini mushrooms",
                    "extra firm tofu",
                    "walnuts",
                    "onion",
                    "garlic",
                    "tomato sauce",
                    "olive oil",
                    "all-purpose flour",
                    "soy milk",
                    "Dijon mustard",
                    "nutritional yeast",
                    "maple syrup",
                    "spinach",
                    "fresh basil"
                ),
                instructions = listOf(
                    "Blend tofu, walnuts, spinach, and seasonings to form vegan ricotta.",
                    "Sauté mushrooms, tofu–walnut mixture, and tomato sauce to create bolognese base.",
                    "Whisk flour, soy milk, mustard, nutritional yeast, and seasonings into a creamy cheese sauce.",
                    "Layer noodles, bolognese, ricotta, and cheese sauce in a casserole; bake at 180 °C for 45 minutes."
                ),
                imageUrl = null,
                calories = 673
            )
        )
        for (meal in denseVeganMeals) {
            db.collection("meals").document(meal.id).set(meal)
        }
        val lightVegetarianMeals = listOf(
            Meal(
                id = "meal_caprese_avocado_toast",
                name = "Avocado & Tomato Caprese Toast",
                description = "Toasted bread topped with mashed avocado, fresh mozzarella, cherry tomatoes, and basil, drizzled with olive oil and balsamic glaze.",
                dietType = "vegetarian",
                density = "light",
                ingredients = listOf(
                    "whole grain bread",
                    "ripe avocado",
                    "cherry tomatoes",
                    "fresh mozzarella",
                    "fresh basil leaves",
                    "olive oil",
                    "balsamic glaze",
                    "salt",
                    "black pepper"
                ),
                instructions = listOf(
                    "Toast the bread until golden.","Mash avocado with salt and spread on toast.","Top with mozzarella, tomato slices, and basil.","Drizzle with olive oil and balsamic glaze before serving."
                ),
                imageUrl = null,
                calories = 300
            ),

            Meal(
                id = "meal_fresh_greek_salad",
                name = "Fresh Greek Salad",
                description = "A crisp salad of cucumber, tomato, red onion, Kalamata olives, and feta tossed in a lemon‑oregano vinaigrette.",
                dietType = "vegetarian",
                density = "light",
                ingredients = listOf(
                    "cucumber",
                    "cherry tomatoes",
                    "red onion",
                    "Kalamata olives",
                    "feta cheese",
                    "olive oil",
                    "lemon juice",
                    "dried oregano",
                    "salt",
                    "black pepper"
                ),
                instructions = listOf(
                    "Chop vegetables and combine in a bowl.","Whisk olive oil, lemon juice, oregano, salt, and pepper.","Pour dressing over salad and toss gently.","Let rest 5 minutes before serving."
                ),
                imageUrl = null,
                calories = 103
            ),

            Meal(
                id = "meal_zucchini_pesto_salad",
                name = "No‑Cook Pesto Zucchini Noodle Salad",
                description = "Spiralized zucchini tossed with basil pesto, cherry tomatoes, and pine nuts for a light, refreshing meal.",
                dietType = "vegetarian",
                density = "light",
                ingredients = listOf(
                    "zucchini",
                    "basil pesto",
                    "cherry tomatoes",
                    "pine nuts",
                    "olive oil",
                    "lemon juice",
                    "salt",
                    "black pepper"
                ),
                instructions = listOf(
                    "Spiralize or peel zucchini into noodles.","Toss zucchini with pesto, halved cherry tomatoes, and pine nuts.","Drizzle with olive oil and lemon juice.","Season with salt and pepper and serve immediately."
                ),
                imageUrl = null,
                calories = 220
            )
        )
        for (meal in lightVegetarianMeals) {
            db.collection("meals").document(meal.id).set(meal)
        }
        val mediumVegetarianMeals = listOf(
            Meal(
                id = "meal_stuffed_shells",
                name = "Spinach & Ricotta Stuffed Shells",
                description = "Medium‑density pasta shells stuffed with creamy ricotta and spinach, baked in marinara and topped with mozzarella.",
                dietType = "vegetarian",
                density = "medium",
                ingredients = listOf(
                    "jumbo pasta shells",
                    "ricotta cheese",
                    "fresh spinach",
                    "egg",
                    "garlic",
                    "salt",
                    "black pepper",
                    "marinara sauce",
                    "shredded mozzarella",
                    "grated Parmesan"
                ),
                instructions = listOf(
                    "Preheat oven to 180 °C.",
                    "Cook pasta shells until al dente; drain and cool slightly.",
                    "Sauté minced garlic and chopped spinach until wilted; let cool.",
                    "Mix spinach, ricotta, beaten egg, Parmesan, salt, and pepper.",
                    "Spread a layer of marinara in a baking dish; stuff each shell with filling.",
                    "Arrange shells in dish, spoon remaining marinara on top, sprinkle with mozzarella.",
                    "Bake 20 minutes until cheese is melted and bubbly."
                ),
                imageUrl = null,
                calories = 620
            ),

            Meal(
                id = "meal_vegetable_korma",
                name = "Vegetable Korma with Basmati Rice",
                description = "A creamy coconut‑based curry loaded with mixed vegetables, served over fragrant basmati rice.",
                dietType = "vegetarian",
                density = "medium",
                ingredients = listOf(
                    "mixed vegetables (carrot, peas, potato, cauliflower)",
                    "onion",
                    "garlic",
                    "ginger",
                    "korma paste",
                    "coconut milk",
                    "plain yogurt",
                    "cashews",
                    "vegetable oil",
                    "salt",
                    "fresh cilantro",
                    "basmati rice"
                ),
                instructions = listOf(
                    "Cook basmati rice according to package instructions.",
                    "Sauté chopped onion, garlic, and ginger in oil until soft.",
                    "Add korma paste and cook 1 minute; add vegetables and stir to coat.",
                    "Pour in coconut milk and yogurt; simmer until vegetables are tender.",
                    "Stir in cashews, season with salt, and garnish with cilantro.",
                    "Serve curry over rice."
                ),
                imageUrl = null,
                calories = 580
            ),

            Meal(
                id = "meal_mushroom_spinach_quesadillas",
                name = "Mushroom & Spinach Quesadillas",
                description = "Cheesy quesadillas filled with sautéed mushrooms and spinach, served with sour cream and salsa.",
                dietType = "vegetarian",
                density = "medium",
                ingredients = listOf(
                    "flour tortillas",
                    "mushrooms",
                    "fresh spinach",
                    "olive oil",
                    "salt",
                    "black pepper",
                    "shredded cheddar cheese",
                    "sour cream",
                    "salsa"
                ),
                instructions = listOf(
                    "Heat oil in a pan; sauté sliced mushrooms and spinach until tender; season with salt and pepper.",
                    "Place a tortilla in another pan over medium heat; sprinkle half with cheese, top with veggie mix and more cheese.",
                    "Fold tortilla and cook 2–3 minutes per side until golden and cheese is melted.",
                    "Cut into wedges and serve with sour cream and salsa."
                ),
                imageUrl = null,
                calories = 640
            )
        )
        for (meal in mediumVegetarianMeals) {
            db.collection("meals").document(meal.id).set(meal)
        }
        val denseVegetarianMeals = listOf(
            Meal(
                id = "meal_four_cheese_baked_ziti",
                name = "Four‑Cheese Baked Ziti",
                description = "A rich and hearty baked pasta layered with mozzarella, ricotta, Parmesan, and cheddar cheeses.",
                dietType = "vegetarian",
                density = "dense",
                ingredients = listOf(
                    "ziti pasta",
                    "ricotta cheese",
                    "mozzarella cheese",
                    "Parmesan cheese",
                    "cheddar cheese",
                    "marinara sauce",
                    "garlic",
                    "olive oil",
                    "salt",
                    "pepper"
                ),
                instructions = listOf(
                    "Preheat oven to 180 °C.",
                    "Cook ziti until al dente; drain and toss with marinara sauce and minced garlic.",
                    "Layer half the pasta in a baking dish, spread dollops of ricotta, sprinkle with mozzarella, Parmesan, and cheddar.",
                    "Repeat layers, top with remaining cheese, and bake 20 minutes until bubbly and golden."
                ),
                imageUrl = null,
                calories = 720
            ),

            Meal(
                id = "meal_black_bean_sweet_potato_enchiladas",
                name = "Black Bean & Sweet Potato Enchiladas",
                description = "Hearty corn tortillas filled with spiced sweet potato and black beans, smothered in enchilada sauce and cheese.",
                dietType = "vegetarian",
                density = "dense",
                ingredients = listOf(
                    "sweet potatoes",
                    "black beans",
                    "corn tortillas",
                    "enchilada sauce",
                    "shredded cheddar cheese",
                    "onion",
                    "garlic",
                    "cumin",
                    "chili powder",
                    "olive oil"
                ),
                instructions = listOf(
                    "Roast diced sweet potatoes with olive oil, cumin, and chili powder until tender.",
                    "Sauté onion and garlic; stir in black beans and roasted sweet potatoes.",
                    "Fill tortillas with mixture, roll up seam‑side down in a baking dish.",
                    "Pour enchilada sauce over tops, sprinkle cheese, and bake at 180 °C for 20 minutes."
                ),
                imageUrl = null,
                calories = 650
            ),

            Meal(
                id = "meal_mushroom_spinach_lasagna",
                name = "Creamy Spinach & Mushroom Lasagna",
                description = "Layers of pasta, sautéed mushrooms and spinach, and a creamy béchamel‑ricotta sauce, baked until golden.",
                dietType = "vegetarian",
                density = "dense",
                ingredients = listOf(
                    "lasagna noodles",
                    "cremini mushrooms",
                    "fresh spinach",
                    "ricotta cheese",
                    "béchamel sauce",
                    "Parmesan cheese",
                    "mozzarella cheese",
                    "olive oil",
                    "garlic",
                    "nutmeg"
                ),
                instructions = listOf(
                    "Preheat oven to 180 °C.",
                    "Sauté mushrooms and garlic in olive oil; add spinach until wilted.",
                    "Spread a layer of béchamel in a baking dish; layer noodles, ricotta, veggies, and béchamel.",
                    "Repeat layers, top with mozzarella and Parmesan, and bake 30 minutes until bubbly."
                ),
                imageUrl = null,
                calories = 700
            )
        )
        for (meal in denseVegetarianMeals) {
            db.collection("meals").document(meal.id).set(meal)
        }
        val lightOmnivoreMeals = listOf(
            Meal(
                id = "meal_turkey_avocado_lettuce_wraps",
                name = "Turkey & Avocado Lettuce Wraps",
                description = "Light and refreshing wraps with lean turkey, creamy avocado, and crisp lettuce.",
                dietType = "omnivore",
                density = "light",
                ingredients = listOf(
                    "large butter lettuce leaves",
                    "sliced turkey breast",
                    "ripe avocado",
                    "tomato",
                    "cucumber",
                    "fresh cilantro",
                    "lime juice",
                    "salt",
                    "pepper"
                ),
                instructions = listOf(
                    "Wash and pat dry lettuce leaves; arrange on a platter.",
                    "Mash avocado with lime juice, salt, and pepper.",
                    "Spread avocado mash on each lettuce leaf.",
                    "Top with turkey slices, tomato, cucumber, and cilantro.",
                    "Roll up gently and serve immediately."
                ),
                imageUrl = null,
                calories = 320
            ),

            Meal(
                id = "meal_shrimp_mango_salad",
                name = "Shrimp & Mango Salad",
                description = "A vibrant salad of grilled shrimp, sweet mango, and mixed greens with a tangy dressing.",
                dietType = "omnivore",
                density = "light",
                ingredients = listOf(
                    "peeled shrimp",
                    "mixed salad greens",
                    "ripe mango",
                    "red bell pepper",
                    "red onion",
                    "olive oil",
                    "lime juice",
                    "honey",
                    "salt",
                    "pepper"
                ),
                instructions = listOf(
                    "Season shrimp with salt and pepper; grill until pink and opaque.",
                    "Whisk olive oil, lime juice, honey, salt, and pepper to make dressing.",
                    "Toss greens, diced mango, bell pepper, and onion with dressing.",
                    "Top with grilled shrimp and serve."
                ),
                imageUrl = null,
                calories = 340
            ),

            Meal(
                id = "meal_chicken_vegetable_skewers",
                name = "Grilled Chicken & Vegetable Skewers",
                description = "Colorful skewers of chicken, bell peppers, zucchini, and onion, lightly seasoned and grilled.",
                dietType = "omnivore",
                density = "light",
                ingredients = listOf(
                    "chicken breast",
                    "bell peppers",
                    "zucchini",
                    "red onion",
                    "olive oil",
                    "garlic powder",
                    "dried oregano",
                    "salt",
                    "pepper",
                    "wooden skewers"
                ),
                instructions = listOf(
                    "Cut chicken and vegetables into bite‑sized pieces.",
                    "Toss with olive oil, garlic powder, oregano, salt, and pepper.",
                    "Thread onto skewers alternating chicken and veggies.",
                    "Grill skewers over medium heat for 10–12 minutes, turning occasionally."
                ),
                imageUrl = null,
                calories = 360
            )
        )
        for (meal in lightOmnivoreMeals) {
            db.collection("meals").document(meal.id).set(meal)
        }
        val mediumOmnivoreMeals = listOf(
            // Source: Quick Beef Stir‑Fry – Allrecipes :contentReference[oaicite:3]{index=3}, Savory stir‑fry technique :contentReference[oaicite:4]{index=4}
            Meal(
                id = "meal_beef_vegetable_stirfry",
                name = "Quick Beef & Vegetable Stir‑Fry",
                description = "Tender beef strips stir‑fried with broccoli, bell peppers, and snap peas in a savory soy‑sesame sauce.",
                dietType = "omnivore",
                density = "medium",
                ingredients = listOf(
                    "sirloin steak",
                    "broccoli florets",
                    "bell pepper",
                    "snap peas",
                    "soy sauce",
                    "sesame oil",
                    "garlic",
                    "ginger",
                    "cornstarch",
                    "vegetable oil",
                    "sesame seeds"
                ),
                instructions = listOf(
                    "Toss sliced beef in cornstarch and season lightly with salt and pepper.",
                    "Heat vegetable oil in a wok; sear beef until browned, then remove.",
                    "Stir‑fry garlic, ginger, and vegetables until crisp‑tender.",
                    "Return beef to wok, add soy sauce and sesame oil; stir until sauce coats everything.",
                    "Garnish with sesame seeds and serve over rice or noodles."
                ),
                imageUrl = null,
                calories = 550
            ),

            // Source: Harvest Salmon Bowls – Allrecipes :contentReference[oaicite:5]{index=5}, Salmon Quinoa Bowl – Allrecipes :contentReference[oaicite:6]{index=6}
            Meal(
                id = "meal_harvest_salmon_bowl",
                name = "Harvest Salmon & Quinoa Bowl",
                description = "Roasted salmon and autumn vegetables served over quinoa, finished with feta, cranberries, and pistachios.",
                dietType = "omnivore",
                density = "medium",
                ingredients = listOf(
                    "salmon fillet",
                    "quinoa",
                    "Brussels sprouts",
                    "broccoli",
                    "feta cheese",
                    "dried cranberries",
                    "pistachios",
                    "olive oil",
                    "salt",
                    "pepper"
                ),
                instructions = listOf(
                    "Roast salmon and vegetables on a sheet pan with olive oil, salt, and pepper at 200 °C until cooked.",
                    "Cook quinoa according to package instructions; fluff with a fork.",
                    "Assemble bowls: quinoa base, roasted veggies, salmon flakes.",
                    "Top with crumbled feta, cranberries, and pistachios."
                ),
                imageUrl = null,
                calories = 600
            ),

            // Source: Pesto Pasta with Chicken – Allrecipes :contentReference[oaicite:7]{index=7}, Spence’s Pesto Chicken Pasta – Allrecipes :contentReference[oaicite:8]{index=8}
            Meal(
                id = "meal_chicken_pesto_pasta",
                name = "Chicken Pesto Pasta",
                description = "Grilled chicken tossed with bow‑tie pasta, sun‑dried tomatoes, and fresh basil pesto for a flavorful pasta dish.",
                dietType = "omnivore",
                density = "medium",
                ingredients = listOf(
                    "bow-tie (farfalle) pasta",
                    "chicken breast",
                    "basil pesto",
                    "sun-dried tomatoes",
                    "Parmesan cheese",
                    "olive oil",
                    "salt",
                    "pepper"
                ),
                instructions = listOf(
                    "Cook pasta until al dente; drain, reserving a little pasta water.",
                    "Grill or sauté seasoned chicken; slice into strips.",
                    "Toss pasta with pesto, sun‑dried tomatoes, and a splash of reserved pasta water.",
                    "Top with chicken strips and grated Parmesan before serving."
                ),
                imageUrl = null,
                calories = 630
            )
        )
        for (meal in mediumOmnivoreMeals) {
            db.collection("meals").document(meal.id).set(meal)
        }
        val denseOmnivoreMeals = listOf(
            Meal(
                id = "meal_meat_lovers_mac_cheese",
                name = "Meat Lover’s Mac & Cheese",
                description = "A rich, indulgent mac & cheese loaded with bacon, sausage, and ground beef in a creamy cheese sauce.",
                dietType = "omnivore",
                density = "dense",
                ingredients = listOf(
                    "elbow pasta",
                    "bacon",
                    "Italian sausage",
                    "ground beef",
                    "butter",
                    "all-purpose flour",
                    "whole milk",
                    "cheddar cheese",
                    "mozzarella cheese",
                    "salt",
                    "pepper",
                    "bread crumbs"
                ),
                instructions = listOf(
                    "Cook pasta until al dente; drain and set aside.",
                    "In a large skillet, cook diced bacon, sausage, and ground beef until browned; drain excess fat.",
                    "In a separate pot, melt butter, whisk in flour to form a roux, then slowly whisk in milk until smooth.",
                    "Stir in shredded cheddar and mozzarella until melted; season with salt and pepper.",
                    "Combine pasta, meat mixture, and cheese sauce; transfer to a baking dish.",
                    "Top with bread crumbs, bake at 180 °C for 20 minutes until bubbly and golden."
                ),
                imageUrl = null,
                calories = 880
            ),

            Meal(
                id = "meal_chicken_alfredo_pasta",
                name = "Creamy Chicken Alfredo Pasta",
                description = "Fettuccine tossed in a rich Parmesan‑cream sauce topped with grilled chicken slices.",
                dietType = "omnivore",
                density = "dense",
                ingredients = listOf(
                    "fettuccine pasta",
                    "chicken breast",
                    "butter",
                    "garlic",
                    "heavy cream",
                    "Parmesan cheese",
                    "olive oil",
                    "salt",
                    "pepper",
                    "parsley"
                ),
                instructions = listOf(
                    "Cook fettuccine until al dente; drain, reserving some pasta water.",
                    "Season chicken with salt and pepper; grill or pan‑sear until cooked through; slice thinly.",
                    "In a pan, melt butter, sauté minced garlic for 1 minute, add heavy cream and simmer.",
                    "Stir in grated Parmesan until smooth; season with salt and pepper.",
                    "Toss pasta in sauce, adding reserved pasta water as needed for consistency.",
                    "Top with sliced chicken and garnish with chopped parsley."
                ),
                imageUrl = null,
                calories = 710
            ),

            Meal(
                id = "meal_beef_bean_burrito_bowl",
                name = "Beef & Bean Burrito Bowl",
                description = "A hearty bowl of seasoned ground beef, black beans, rice, cheese, and guacamole.",
                dietType = "omnivore",
                density = "dense",
                ingredients = listOf(
                    "ground beef",
                    "taco seasoning",
                    "white rice",
                    "black beans",
                    "shredded cheddar cheese",
                    "avocado",
                    "tomato",
                    "lettuce",
                    "sour cream",
                    "lime",
                    "salt",
                    "pepper"
                ),
                instructions = listOf(
                    "Cook rice according to package instructions; fluff with a fork.",
                    "Brown ground beef in a skillet, drain fat, stir in taco seasoning and a splash of water.",
                    "Warm black beans in a small pot; season lightly with salt and pepper.",
                    "Assemble bowl: rice base, seasoned beef, black beans, shredded cheese, diced tomato, and lettuce.",
                    "Top with diced avocado (or guacamole), a dollop of sour cream, and a squeeze of lime."
                ),
                imageUrl = null,
                calories = 750
            )
        )
        for (meal in denseOmnivoreMeals) {
            db.collection("meals").document(meal.id).set(meal)
        }
    }
}
