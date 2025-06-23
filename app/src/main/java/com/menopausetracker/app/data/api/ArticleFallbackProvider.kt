package com.menopausetracker.app.data.api

import com.menopausetracker.app.data.model.Article
import java.util.Calendar
import java.util.Date
import java.util.UUID

/**
 * Provides pre-loaded fallback articles when network sources are unavailable
 */
class ArticleFallbackProvider {
    companion object {
        /**
         * Returns a list of pre-defined articles about menopause and women's health
         */
        fun getFallbackArticles(): List<Article> {
            val calendar = Calendar.getInstance()
            val articles = mutableListOf<Article>()

            // Article 1
            calendar.add(Calendar.DAY_OF_YEAR, -2)
            articles.add(Article(
                id = UUID.randomUUID().toString(),
                title = "Understanding Menopause: A Complete Guide",
                summary = "Menopause is a natural biological process that marks the end of a woman's menstrual cycles. Learn about symptoms, management strategies, and what to expect during this transition.",
                content = """
                    Menopause is a significant transition in a woman's life, typically occurring between ages 45 and 55. 
                    
                    Common symptoms include:
                    • Hot flashes and night sweats
                    • Sleep disturbances
                    • Mood changes and irritability
                    • Vaginal dryness
                    • Decreased sex drive
                    
                    Management strategies that can help:
                    
                    1. Lifestyle modifications - Regular exercise, maintaining a healthy diet, and practicing stress reduction techniques can significantly ease menopause symptoms.
                    
                    2. Staying cool - Dressing in layers, using fans, and keeping your bedroom cool at night can help manage hot flashes and night sweats.
                    
                    3. Adequate sleep - Establish a regular sleep schedule and create a comfortable sleep environment.
                    
                    4. Pelvic floor exercises - These can help maintain bladder control and sexual function.
                    
                    5. Open communication - Talking with healthcare providers, partners, and friends can provide both emotional support and practical advice.
                    
                    Remember that menopause is a natural process, not a disorder. With proper management, most women navigate this transition successfully and go on to enjoy healthy post-menopausal years.
                """,
                imageUrl = null,
                sourceUrl = "https://www.mayoclinic.org/diseases-conditions/menopause/symptoms-causes/syc-20353397",
                sourceName = "Mayo Clinic (Offline Article)",
                publishDate = calendar.time,
                category = "Menopause",
                readTimeMinutes = 4
            ))

            // Article 2
            calendar.add(Calendar.DAY_OF_YEAR, -5)
            articles.add(Article(
                id = UUID.randomUUID().toString(),
                title = "Natural Remedies for Hot Flashes",
                summary = "Hot flashes are one of the most common symptoms of menopause. Discover science-backed natural remedies that can help reduce their frequency and intensity.",
                content = """
                    Hot flashes affect up to 80% of women during menopause. These sudden feelings of warmth can be accompanied by sweating, flushing, and heart palpitations.
                    
                    While hormone replacement therapy is effective, many women prefer natural approaches first. Here are evidence-based natural remedies:
                    
                    • Black cohosh: Some studies suggest this herb may reduce hot flash frequency, though results are mixed.
                    
                    • Evening primrose oil: Contains gamma-linolenic acid, which might help balance hormones.
                    
                    • Flaxseeds: Rich in lignans that have weak estrogenic effects and may reduce hot flash severity.
                    
                    • Mindfulness meditation: Regular practice can reduce stress and the perception of hot flashes.
                    
                    • Acupuncture: Some women report relief with regular sessions.
                    
                    • Cooling strategies: Dress in layers, carry a portable fan, avoid triggers like spicy food and alcohol.
                    
                    • Regular exercise: Moderate physical activity can help regulate body temperature and improve overall well-being.
                    
                    Always consult your healthcare provider before starting any supplement regimen, especially if you have existing health conditions or take medications.
                """,
                imageUrl = null,
                sourceUrl = "https://www.nih.gov/health-information",
                sourceName = "National Institutes of Health (Offline Article)",
                publishDate = calendar.time,
                category = "Menopause",
                readTimeMinutes = 5
            ))

            // Article 3
            calendar.add(Calendar.DAY_OF_YEAR, -8)
            articles.add(Article(
                id = UUID.randomUUID().toString(),
                title = "Exercise and Menopause: Finding the Right Balance",
                summary = "Regular physical activity offers numerous benefits during menopause, from reducing symptoms to supporting bone health. Learn how to create an effective exercise routine.",
                content = """
                    Exercise is one of the most effective ways to manage menopause symptoms while protecting your long-term health. Research shows that active women experience fewer hot flashes and sleep better during this transition.
                    
                    Benefits of exercise during menopause:
                    
                    • Reduces hot flashes and night sweats
                    • Improves sleep quality
                    • Helps maintain healthy weight
                    • Strengthens bones, reducing osteoporosis risk
                    • Boosts mood and reduces anxiety
                    • Enhances overall quality of life
                    
                    Recommended exercise types:
                    
                    1. Aerobic exercise: 150 minutes of moderate activity weekly (walking, swimming, cycling)
                    
                    2. Strength training: 2-3 sessions weekly to maintain muscle mass and bone density
                    
                    3. Balance exercises: Yoga or tai chi to improve stability and flexibility
                    
                    4. Pelvic floor exercises: Kegels to help with urinary incontinence
                    
                    Starting tips:
                    • Begin gradually and increase intensity slowly
                    • Choose activities you enjoy
                    • Exercise with friends for motivation and social benefits
                    • Stay hydrated and dress in cooling fabrics
                    
                    Always consult your healthcare provider before starting a new exercise program, particularly if you have existing health conditions.
                """,
                imageUrl = null,
                sourceUrl = "https://www.womenshealth.gov/menopause/menopause-and-your-health",
                sourceName = "Office on Women's Health (Offline Article)",
                publishDate = calendar.time,
                category = "Women's Health",
                readTimeMinutes = 6
            ))

            // Article 4
            calendar.add(Calendar.DAY_OF_YEAR, -12)
            articles.add(Article(
                id = UUID.randomUUID().toString(),
                title = "Nutrition for Menopause: Foods That Help and Harm",
                summary = "Your diet can significantly impact how you experience menopause. Discover which foods may help alleviate symptoms and which ones you might want to avoid.",
                content = """
                    What you eat during menopause can either worsen or improve your symptoms. Certain foods contain compounds that may help balance hormones and reduce discomfort.
                    
                    Foods that may help:
                    
                    • Phytoestrogen-rich foods: Soy products, flaxseeds, and legumes contain plant compounds that mimic estrogen's effects and may reduce hot flashes.
                    
                    • Calcium and vitamin D sources: Dairy products, fortified plant milks, leafy greens, and fatty fish support bone health when estrogen levels decline.
                    
                    • Omega-3 fatty acids: Found in fatty fish, walnuts, and flaxseeds, these may improve mood and heart health.
                    
                    • Fruits and vegetables: Rich in antioxidants that help combat oxidative stress during this hormonal transition.
                    
                    • Whole grains: Provide B vitamins that may help with energy levels and mood regulation.
                    
                    Foods to limit:
                    
                    • Spicy foods: Can trigger hot flashes in some women.
                    
                    • Caffeine and alcohol: May disrupt sleep and worsen hot flashes.
                    
                    • Added sugars: Can contribute to mood swings and fatigue.
                    
                    • Highly processed foods: Often high in sodium, which may increase bloating and water retention.
                    
                    Eating smaller, more frequent meals can help maintain stable blood sugar levels, which may also help manage mood and energy fluctuations during menopause.
                """,
                imageUrl = null,
                sourceUrl = "https://www.medlineplus.gov/menopause.html",
                sourceName = "MedlinePlus (Offline Article)",
                publishDate = calendar.time,
                category = "Women's Health",
                readTimeMinutes = 5
            ))

            // Article 5
            calendar.add(Calendar.DAY_OF_YEAR, -15)
            articles.add(Article(
                id = UUID.randomUUID().toString(),
                title = "Maintaining Bone Health After Menopause",
                summary = "The decline in estrogen during menopause accelerates bone loss, increasing osteoporosis risk. Learn strategies to keep your bones strong and healthy.",
                content = """
                    Bone health becomes a critical concern after menopause due to declining estrogen levels. Women can lose up to 20% of their bone density in the five to seven years following menopause, making osteoporosis prevention essential.
                    
                    Key strategies for maintaining bone health:
                    
                    1. Calcium-rich diet: Aim for 1,200 mg daily from foods like dairy, fortified plant milks, leafy greens, and canned fish with bones.
                    
                    2. Vitamin D: Essential for calcium absorption. Sources include sunlight exposure, fatty fish, egg yolks, and fortified foods. Many women need supplementation.
                    
                    3. Weight-bearing exercise: Activities like walking, dancing, tennis, and resistance training stimulate bone formation. Aim for 30 minutes daily.
                    
                    4. Fall prevention: Practice balance exercises, remove home hazards, ensure adequate lighting, and wear proper footwear.
                    
                    5. Bone density testing: The DXA scan is recommended for all women age 65+ and younger postmenopausal women with risk factors.
                    
                    6. Limit bone-depleting habits: Reduce alcohol consumption and avoid smoking, which accelerates bone loss.
                    
                    7. Medications: Discuss with your healthcare provider whether bone-preserving medications might be appropriate based on your risk factors.
                    
                    Taking proactive steps now can significantly reduce fracture risk and maintain mobility and independence as you age.
                """,
                imageUrl = null,
                sourceUrl = "https://www.bones.nih.gov",
                sourceName = "NIH Osteoporosis and Related Bone Diseases (Offline Article)",
                publishDate = calendar.time,
                category = "Health & Aging",
                readTimeMinutes = 5
            ))

            // Article 6
            calendar.add(Calendar.DAY_OF_YEAR, -20)
            articles.add(Article(
                id = UUID.randomUUID().toString(),
                title = "Sleep Solutions for Menopause",
                summary = "Up to 60% of women report sleep disturbances during menopause. Discover effective strategies for getting better rest during this transitional time.",
                content = """
                    Sleep problems are among the most disruptive symptoms of menopause, affecting mood, cognitive function, and overall quality of life. Night sweats, insomnia, and sleep-disordered breathing all become more common during this transition.
                    
                    Effective sleep strategies include:
                    
                    • Optimize your sleep environment: Keep your bedroom cool (65°F/18°C is ideal), dark, and quiet. Use moisture-wicking bedding and sleepwear.
                    
                    • Establish a consistent sleep schedule: Go to bed and wake up at the same times every day, including weekends.
                    
                    • Create a relaxing bedtime routine: Try reading, gentle stretching, or meditation before sleep. Avoid screens at least one hour before bedtime.
                    
                    • Watch what you consume: Limit caffeine after noon, avoid alcohol within 3 hours of bedtime, and avoid large meals before sleep.
                    
                    • Exercise regularly: Physical activity promotes better sleep, but try to finish intense workouts at least 3 hours before bedtime.
                    
                    • Manage stress: Practices like mindfulness meditation, deep breathing, or yoga can help calm an active mind.
                    
                    • Consider cognitive behavioral therapy for insomnia (CBT-I): This evidence-based approach helps retrain your brain for better sleep.
                    
                    If sleep problems persist despite these measures, speak with your healthcare provider. Addressing sleep issues during menopause isn't just about comfort—it's essential for your long-term health.
                """,
                imageUrl = null,
                sourceUrl = "https://www.sleepfoundation.org/women-sleep/menopause-and-sleep",
                sourceName = "Sleep Foundation (Offline Article)",
                publishDate = calendar.time,
                category = "Menopause",
                readTimeMinutes = 4
            ))

            return articles
        }
    }
}
