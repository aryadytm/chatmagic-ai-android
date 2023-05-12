package dev.bytebooster.chatmagicai.data

import dev.bytebooster.chatmagicai.R
import dev.bytebooster.chatmagicai.model.AiModel
import dev.bytebooster.chatmagicai.model.ChatMessage
import dev.bytebooster.chatmagicai.model.Template


class FakeDatasource {

    fun loadModels(): List<AiModel> {
        return listOf(
            AiModel(
                name = "ChatMagic AI Small",
                description = "The most lightweight AI model. It only uses 150 MB of device storage and consumes 350 MB RAM.",
                downloadUrl = "https://",
                size = 1024,
                id = 2023020801,
                type = "s",
                formatter = "formatter_convpair_askth",
            ),
            AiModel(
                name = "ChatMagic AI Medium",
                description = "This AI model is more accurate and gives better responses than the small one. It uses 450 MB device storage and needs minimum 1 GB free RAM.",
                downloadUrl = "https://",
                size = 1024,
                id = 2023021102,
                type = "m",
                formatter = "formatter_convpair_askth",

            ),
            AiModel(
                name = "ChatMagic AI Large (Coming Soon)",
                description = "Currently the most accurate AI model for mobile. It gives the best responses than any other models. It uses 1 GB storage and 2 GB RAM.",
                downloadUrl = "https://",
                size = 1024,
                id = 2023021003,
                type = "l",
                formatter = "formatter_convpair_askth",
            ),
        )
    }

    fun loadTemplates(): List<Template> {
        return listOf(
            Template(
                title = "Homework Solver",
                desc = "Assists you solving the homework or assignments",
                image = R.drawable.thumbnail_1
            ),
            Template(
                title = "Social Media Creator",
                desc = "Write engaging tweets or captions",
                image = R.drawable.thumbnail_1
            ),
            Template(
                title = "Writing Assistant",
                desc = "Write about anything in seconds",
                image = R.drawable.thumbnail_1
            ),
            Template(
                title = "Fitness Coach",
                desc = "Ask about the best fitness plan",
                image = R.drawable.thumbnail_1
            ),
            Template(
                title = "Master Chef",
                desc = "Get recipes for any food with any ingredients",
                image = R.drawable.thumbnail_1
            ),
            Template(
                title = "Script Writer",
                desc = "Write voiceover or dubbing scripts",
                image = R.drawable.thumbnail_1
            ),
            Template(
                title = "Grammar Checker",
                desc = "Correct your English text",
                image = R.drawable.thumbnail_1
            ),
            Template(
                title = "Movie Actor",
                desc = "Act as an actor or actress in movie",
                image = R.drawable.thumbnail_1
            ),
        )
    }

    fun loadChats(): List<ChatMessage> {
        return listOf(
            ChatMessage(
                sender = "You",
                content = "Please write a letter to my teacher that informs I'm sick so I can't go to school.",
                senderAvatar = R.drawable.ic_user,
                isUser = true,
            ),
            ChatMessage(
                sender = "ChatMagic AI",
                content = "Dear [Teacher's Name],\n" +
                        "I hope this letter finds you in good health.\n" +
                        "\n" +
                        "I am writing to inform you that I am feeling unwell and, as a result, will not be able to attend school today. I have consulted a doctor and they have advised me to stay at home and rest until I am fully recovered.\n" +
                        "\n" +
                        "Thank you for your understanding and I hope to be back in school soon",
                senderAvatar = R.drawable.ic_chatmagicai,
                isUser = false,
            ),
//            ChatMessage(
//                sender = "You",
//                content = "Who is Elon Musk?",
//                senderAvatar = R.drawable.ic_user,
//                isUser = true,
//            ),
//            ChatMessage(
//                sender = "ChatMagic AI",
//                content = "Elon Musk is a billionaire entrepreneur and business magnate. He is the CEO and co-founder of Tesla Motors, a company specializing in electric cars, and SpaceX, an aerospace manufacturer and space transport services company. He is also the founder of The Boring Company, Neuralink and OpenAI. He is known for his ambitious goals of reducing global warming through sustainable energy and colonizing Mars.",
//                senderAvatar = R.drawable.ic_chatmagicai,
//                isUser = false,
//            ),
            ChatMessage(
                sender = "You",
                content = "How to make a pizza with just 3 ingredients?",
                senderAvatar = R.drawable.ic_user,
                isUser = true,
            ),
            ChatMessage(
                sender = "ChatMagic AI",
                content = "To make a pizza with just 3 ingredients, you will need:\n" +
                        "\n" +
                        "- Flatbread or pita bread\n" +
                        "- Tomato sauce\n" +
                        "- Shredded mozzarella cheese\n\n" +
                        "Instructions:\n" +
                        "\n" +
                        "1. Preheat your oven to 450°F (230°C).\n" +
                        "2. Place the flatbread or pita bread on a baking sheet or pizza stone.\n" +
                        "3. Spoon tomato sauce over the flatbread, spreading it evenly to cover the surface.\n" +
                        "4. Sprinkle shredded mozzarella cheese over the tomato sauce.\n" +
                        "5. Bake in the oven for 8-10 minutes, or until the cheese is melted and the crust is crispy.\n" +
                        "6. Remove from the oven, slice, and serve hot.\n\n" +
                        "Note: You can add additional toppings such as fresh basil, black pepper, or red pepper flakes to taste.",
                senderAvatar = R.drawable.ic_chatmagicai,
                isUser = false,
            ),
        )
    }

}