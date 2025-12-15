package ee.mips.beerreal.data

import ee.mips.beerreal.data.model.BeerPost
import ee.mips.beerreal.data.model.Comment
import ee.mips.beerreal.data.model.User
import ee.mips.beerreal.data.model.VoteType
import java.util.Calendar
import java.util.Date

object MockData {
    // AI generated mock data for demonstration and testing purposes
    val currentUser = User(
        id = "user1",
        username = "beermanAlrightman",
        email = "beerman@example.com",
        profileImageUrl = null,
        tasteScore = 850,
        totalPosts = 42,
        friendsCount = 156,
        bio = "Dad, husband, President, citizen."
    )
    
    private val users = listOf(
        User(
            id = "user2",
            username = "hophead_jane",
            email = "jane@example.com",
            tasteScore = 920,
            totalPosts = 38,
            friendsCount = 89
        ),
        User(
            id = "user3",
            username = "lager_lover",
            email = "mike@example.com",
            tasteScore = 650,
            totalPosts = 25,
            friendsCount = 67
        ),
        User(
            id = "user4",
            username = "stout_sam",
            email = "sam@example.com",
            tasteScore = 780,
            totalPosts = 31,
            friendsCount = 134
        )
    )
    
    private fun getRandomDate(): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -(0..7).random())
        calendar.add(Calendar.HOUR_OF_DAY, -(0..23).random())
        return calendar.time
    }
    
    val mockBeerPosts = listOf(
        BeerPost(
            id = "post1",
            userId = "user2",
            username = "hophead_jane",
            caption = "IPA Supreme from Craft Valley Brewery - Perfect hoppy balance! 🍺",
            imageData = "", // Mock data doesn't have real base64 images
            location = "The Hop House",
            timestamp = getRandomDate(),
            upvotes = 24,
            downvotes = 3,
            comments = listOf(
                Comment(
                    id = "comment1",
                    userId = "user1",
                    username = "beermaster",
                    text = "Great choice! Love this brewery",
                    timestamp = getRandomDate()
                ),
                Comment(
                    id = "comment2",
                    userId = "user3",
                    username = "lager_lover",
                    text = "Too hoppy for me but looks good!",
                    timestamp = getRandomDate()
                )
            ),
            userVoteType = VoteType.UPVOTE
        ),
        BeerPost(
            id = "post2",
            userId = "user3",
            username = "lager_lover",
            caption = "Golden Lager by Traditional Brew Co. - Clean and crisp, perfect for a sunny day",
            imageData = "",
            location = "Beach Bar & Grill",
            timestamp = getRandomDate(),
            upvotes = 18,
            downvotes = 2,
            comments = listOf(
                Comment(
                    id = "comment3",
                    userId = "user4",
                    username = "stout_sam",
                    text = "Classic choice! 👍",
                    timestamp = getRandomDate()
                )
            )
        ),
        BeerPost(
            id = "post3",
            userId = "user4",
            username = "stout_sam",
            caption = "Midnight Stout from Dark Horse Brewing - Rich chocolate and coffee notes. Amazing!",
            imageData = "",
            location = "The Cellar",
            timestamp = getRandomDate(),
            upvotes = 31,
            downvotes = 1,
            comments = listOf(
                Comment(
                    id = "comment4",
                    userId = "user1",
                    username = "beermaster",
                    text = "One of my favorites! The coffee notes are incredible",
                    timestamp = getRandomDate()
                ),
                Comment(
                    id = "comment5",
                    userId = "user2",
                    username = "hophead_jane",
                    text = "Love a good stout 🖤",
                    timestamp = getRandomDate()
                )
            ),
            userVoteType = VoteType.UPVOTE
        ),
        BeerPost(
            id = "post4",
            userId = "user1",
            username = "beermaster",
            caption = "Wheat Wonder by Sunny Skies Brewery - Light and refreshing with a hint of citrus",
            imageData = "",
            location = "Rooftop Lounge",
            timestamp = getRandomDate(),
            upvotes = 15,
            downvotes = 5,
            comments = emptyList()
        ),
        BeerPost(
            id = "post5",
            userId = "user2",
            username = "hophead_jane",
            caption = "Double IPA Madness from Extreme Hops Co. - Hop bomb! Not for the faint of heart 💥",
            imageData = "",
            location = "Craft Beer Corner",
            timestamp = getRandomDate(),
            upvotes = 28,
            downvotes = 8,
            comments = listOf(
                Comment(
                    id = "comment6",
                    userId = "user3",
                    username = "lager_lover",
                    text = "Way too intense for me 😅",
                    timestamp = getRandomDate()
                )
            )
        ),
        // More posts for current user (user1) for profile demonstration
        BeerPost(
            id = "post6",
            userId = "user1",
            username = "beermaster",
            caption = "Pilsner Perfection - Czech style lager that's absolutely divine ✨",
            imageData = "",
            location = "The Beer Garden",
            timestamp = getRandomDate(),
            upvotes = 22,
            downvotes = 1,
            comments = emptyList()
        ),
        BeerPost(
            id = "post7",
            userId = "user1",
            username = "beermaster",
            caption = "Bourbon Barrel Aged Porter - Rich, complex, and warming. Perfect for a cold evening! 🥃",
            imageData = "",
            location = "Whiskey & Hops",
            timestamp = getRandomDate(),
            upvotes = 35,
            downvotes = 2,
            comments = listOf(
                Comment(
                    id = "comment7",
                    userId = "user4",
                    username = "stout_sam",
                    text = "Bourbon barrel aging is the best! 🔥",
                    timestamp = getRandomDate()
                )
            )
        ),
        BeerPost(
            id = "post8",
            userId = "user1",
            username = "beermaster",
            caption = "Sour Cherry Ale - Tart, fruity, and refreshing. Summer vibes in a glass! 🍒",
            imageData = "",
            location = "Sour House Brewery",
            timestamp = getRandomDate(),
            upvotes = 19,
            downvotes = 6,
            comments = emptyList()
        ),
        BeerPost(
            id = "post9",
            userId = "user1",
            username = "beermaster",
            caption = "Imperial Stout - Bold, coffee notes, and 12% ABV. Not messing around tonight! ☕",
            imageData = "",
            location = "Dark Side Brewing",
            timestamp = getRandomDate(),
            upvotes = 41,
            downvotes = 3,
            comments = listOf(
                Comment(
                    id = "comment8",
                    userId = "user2",
                    username = "hophead_jane",
                    text = "That's a strong one! Respect 💪",
                    timestamp = getRandomDate()
                ),
                Comment(
                    id = "comment9",
                    userId = "user3",
                    username = "lager_lover",
                    text = "Too strong for me but looks amazing",
                    timestamp = getRandomDate()
                )
            )
        ),
        BeerPost(
            id = "post10",
            userId = "user1",
            username = "beermaster",
            caption = "Hazy IPA - Juicy, tropical, and cloudy. New England style at its finest! 🌴",
            imageData = "",
            location = "Tropical Hops Co.",
            timestamp = getRandomDate(),
            upvotes = 27,
            downvotes = 1,
            comments = emptyList()
        ),
        BeerPost(
            id = "post11",
            userId = "user1",
            username = "beermaster",
            caption = "Belgian Tripel - Complex, spicy, and strong. Traditional brewing at its best 🇧🇪",
            imageData = "",
            location = "Abbey Ales",
            timestamp = getRandomDate(),
            upvotes = 33,
            downvotes = 2,
            comments = listOf(
                Comment(
                    id = "comment10",
                    userId = "user4",
                    username = "stout_sam",
                    text = "Belgian beers are underrated!",
                    timestamp = getRandomDate()
                )
            )
        ),
        BeerPost(
            id = "post12",
            userId = "user1",
            username = "beermaster",
            caption = "Smoked Porter - Unique smoky flavor that pairs perfectly with BBQ 🔥",
            imageData = "",
            location = "Smokehouse Brewery",
            timestamp = getRandomDate(),
            upvotes = 24,
            downvotes = 4,
            comments = emptyList()
        ),
        BeerPost(
            id = "post13",
            userId = "user1",
            username = "beermaster",
            caption = "Gose with Sea Salt - Salty, sour, and refreshing. Beach beer vibes! 🌊",
            imageData = "",
            location = "Seaside Brewing",
            timestamp = getRandomDate(),
            upvotes = 18,
            downvotes = 7,
            comments = emptyList()
        ),
        BeerPost(
            id = "post14",
            userId = "user1",
            username = "beermaster",
            caption = "West Coast IPA - Bitter, piney, and classic. Old school hop character! 🌲",
            imageData = "",
            location = "Coast Brewing",
            timestamp = getRandomDate(),
            upvotes = 31,
            downvotes = 2,
            comments = listOf(
                Comment(
                    id = "comment11",
                    userId = "user2",
                    username = "hophead_jane",
                    text = "West Coast > New England 🙌",
                    timestamp = getRandomDate()
                )
            )
        ),
        BeerPost(
            id = "post15",
            userId = "user1",
            username = "beermaster",
            caption = "Pumpkin Spice Ale - Seasonal perfection with cinnamon and nutmeg 🎃",
            imageData = "",
            location = "Autumn Brews",
            timestamp = getRandomDate(),
            upvotes = 26,
            downvotes = 8,
            comments = emptyList()
        ),
        BeerPost(
            id = "post16",
            userId = "user1",
            username = "beermaster",
            caption = "Russian Imperial Stout - Dark, rich, and warming. Winter warrior beer! ❄️",
            imageData = "",
            location = "Winter Ales House",
            timestamp = getRandomDate(),
            upvotes = 39,
            downvotes = 1,
            comments = listOf(
                Comment(
                    id = "comment12",
                    userId = "user4",
                    username = "stout_sam",
                    text = "Now THIS is my kind of beer! 🖤",
                    timestamp = getRandomDate()
                )
            )
        )
    )
    
    fun getBeerPosts(): List<BeerPost> = mockBeerPosts
    
    fun getUserPosts(userId: String): List<BeerPost> = 
        mockBeerPosts.filter { it.userId == userId }
    
    fun getUserById(userId: String): User? = 
        if (userId == currentUser.id) currentUser else users.find { it.id == userId }
    
    fun getAllUsers(): List<User> = listOf(currentUser) + users
}
