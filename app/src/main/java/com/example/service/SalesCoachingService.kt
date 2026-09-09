package com.example.service

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ObjectionArticle(
    val title: String,
    val objectionText: String,
    val whyItHappens: String,
    val bestResponseScript: String,
    val practicalTip: String
)

data class LeadSuggestion(
    val industry: String,
    val targetPersona: String,
    val corePainPoint: String,
    val openingPitchHook: String,
    val bestLeadSources: List<String>,
    val recommendedSampleCompanyName: String,
    val recommendedContactName: String
)

object SalesCoachingService {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // 1. Static high-value objection handling library
    val objectionLibrary: List<ObjectionArticle> = listOf(
        ObjectionArticle(
            title = "اعتراض ۱: خیلی گرونه / تخفیف میخوایم",
            objectionText = "«قیمت‌تون خیلی بالاست، از شرکت‌های دیگه قیمت پایین‌تر گرفتیم!»",
            whyItHappens = "مشتری هنوز تفاوت ارزش دریافتی و بازگشت سرمایه (ROI) را درک نکرده و فقط به هزینه نگاه می‌کند.",
            bestResponseScript = "«کاملاً حق با شماست، قیمت یکی از فاکتورهای کلیدی هر خریده. ولی اجازه بدین بپرسم، وقتی مشتریان قبلی ما اولش همین نظر رو داشتن، چی شد که ما رو انتخاب کردن؟ به خاطر اینکه کیفیت پشتیبانی و ضمانت بازگشت سرمایه ما از هزینه‌های مجدد در آینده جلوگیری می‌کنه. اگر بتونم نشون بدم این راه‌حل چطور در ۶ ماه هزینه‌هاتون رو ۳۰٪ کاهش میده، مایلید جزئیاتش رو با هم ببینیم؟»",
            practicalTip = "هرگز بلافاصله تخفیف ندهید! اول ارزش افزوده، خدمات پس از فروش و ریسک خرید ارزان را برجسته کنید."
        ),
        ObjectionArticle(
            title = "اعتراض ۲: الان بودجه نداریم / شرایط اقتصادی خرابه",
            objectionText = "«الان در وضعیت مالی خوبی نیستیم، بذارید برای ۶ ماه دیگه یا سال بعد.»",
            whyItHappens = "مشتری محصول یا خدمات شما را به عنوان هزینه می‌بیند نه نجات‌دهنده یا سودآور.",
            bestResponseScript = "«متوجه چالش‌های بودجه‌ای هستم مهندس. دقیقاً به همین دلیل شرکت‌هایی مثل شما الان با ما همکاری می‌کنند؛ چون اجرای این سیستم به شما کمک می‌کنه هزینه‌های اتلاف شده رو متوقف کنید و نقدینگی بیشتری بسازید. آیا مایلی یک پلن اقساطی یا مرحله‌ای با هم بچینیم که پرداخت‌ها بر اساس نتایج مرحله‌ای باشه؟»",
            practicalTip = "پلن پرداخت چندمرحله‌ای پیشنهاد دهید و روی هزینه عدم اقدام (Cost of Inaction) تمرکز کنید."
        ),
        ObjectionArticle(
            title = "اعتراض ۳: کاتالوگ یا اطلاعات رو بفرستید بررسی می‌کنم",
            objectionText = "«اطلاعات رو تلگرام یا ایمیل کنید، اگه لازم شد خودم زنگ می‌زنم.»",
            whyItHappens = "روشی محترمانه برای پایان دادن به مکالمه بدون تعهد.",
            bestResponseScript = "«حتماً با کمال میل کاتالوگ رو براتون می‌فرستم. فقط چون کاتالوگ ما بیش از ۵۰ صفحه است و نمی‌خوام وقت ارزشمندتون گرفته بشه، در حال حاضر مهم‌ترین دغدغه یا هدفی که در تیمتون دنبال می‌کنید چیه تا فقط بخش مربوط به همون رو براتون ارسال کنم؟»",
            practicalTip = "با ارسال کاتالوگ تماس را نبندید؛ بلافاصله یک تایم مشخص (مثلاً فردا ساعت ۱۱) برای بررسی ۲ دقیقه‌ای هماهنگ کنید."
        ),
        ObjectionArticle(
            title = "اعتراض ۴: با شخص یا شرکت دیگه‌ای کار می‌کنیم",
            objectionText = "«ما الان تامین‌کننده ثابت خودمون رو داریم و نیازی به تغییر نداریم.»",
            whyItHappens = "ترس از ریسک تغییر و جابه‌جایی رابطه‌های قبلی.",
            bestResponseScript = "«خیلی هم عالی! داشتن تامین‌کننده منظم واقعاً ارزشمنده. هدف ما این نیست که همکاری خوب شما رو قطع کنیم؛ بلکه بیشتر مشتریان ما از ما به عنوان تأمین‌کننده دوم (Plan B) استفاده می‌کنند تا همیشه تنوع قیمت و امنیت موجودی داشته باشند. اجازه هست یک سفارش کوچک آزمایشی یا نمونه برای ارزیابی بفرستیم؟»",
            practicalTip = "مستقیماً رقیب را تخریب نکنید. خود را به عنوان گزینه پشتیبان (تأمین‌کننده آلترناتیو) معرفی کنید."
        ),
        ObjectionArticle(
            title = "اعتراض ۵: باید با مدیرعامل یا شریکم مشورت کنم",
            objectionText = "«من تصمیم‌گیرنده نهایی نیستم، باید توی جلسه هیئت‌مدیره مطرح کنم.»",
            whyItHappens = "عدم داشتن اختیار خرید یا عدم تمایل به مسئولیت‌پذیری به تنهایی.",
            bestResponseScript = "«کاملاً منطقیه. شما خودتون نظرتون درباره این راهکار چیه؟ ... عالیه! معمولاً مدیرعامل یا شریک شما چه معیاری رو در اولویت قرار میده؟ سودآوری، زمان تحویل، یا اطمینان؟ من یک خلاصه ۱ صفحه‌ای مخصوص هیئت مدیره برای شما آماده می‌کنم تا دست پر در جلسه حاضر بشید. حتی اگر مایل باشید می‌تونم در جلسه آنلاین ۱۰ دقیقه حضور داشته باشم.»",
            practicalTip = "مخاطب را تبدیل به وکیل مدافع خود در برابر مدیر بالادستی کنید."
        )
    )

    // 2. Curated lead suggestions across key Iranian business industries
    val leadSuggestions: List<LeadSuggestion> = listOf(
        LeadSuggestion(
            industry = "فناوری اطلاعات و استارتاپ‌ها",
            targetPersona = "مدیر عامل (CEO) یا مدیر فنی (CTO) یا مدیر رشد (Growth)",
            corePainPoint = "جذب مشتری با هزینه کمتر (CAC پایین‌تر)، کمبود نیروی تخصصی، مقیاس‌پذیری زیرساخت",
            openingPitchHook = "«سلام جناب مهندس، دیدم اخیراً محصول جدیدتون رو لانچ کردید؛ ما به شرکت‌های نرم‌افزاری کمک می‌کنیم نرخ تبدیل بازدیدکننده به لیدشون رو تا ۳۵٪ افزایش بدن...»",
            bestLeadSources = listOf("لینکدین ایران", "بانک دایرکتوری شرکت‌های دانش‌بنیان", "نمایشگاه الکامپ و اینوتکس", "وبسایت‌های کاریابی مانند جابینجا"),
            recommendedSampleCompanyName = "داده پردازان هوش مصنوعی پارس",
            recommendedContactName = "مهندس نیما شمس (مدیر محصول)"
        ),
        LeadSuggestion(
            industry = "صنایع تولیدی، کارخانجات و بازرگانی",
            targetPersona = "مدیر تدارکات و خرید، مدیر بازرگانی یا مدیر کارخانه",
            corePainPoint = "تأمین مطمئن مواد اولیه، ثبات قیمت، تحویل به موقع در خط تولید، نقدینگی و تسویه اعتباری",
            openingPitchHook = "«سلام مهندس، از شرکت تامین تجهیزات تماس می‌گیرم؛ ما امکان تحویل فوری با ضمانت اصالت و تسویه ۴۵ روزه برای خطوط تولید صنعتی فراهم کردیم...»",
            bestLeadSources = listOf("کتاب اول و دایرکتوری شهرک‌های صنعتی", "نمایشگاه صنعت تهران", "اتاق بازرگانی و صنایع و معادن"),
            recommendedSampleCompanyName = "صنایع مکانیک دقیق البرز",
            recommendedContactName = "حاج محسن کریمی (مدیر بازرگانی)"
        ),
        LeadSuggestion(
            industry = "پزشکی، دارویی و کلینیک‌های سلامت",
            targetPersona = "پزشک موسس، مدیر داخلی کلینیک، مسئول خرید داروخانه",
            corePainPoint = "تاییدیه و مجوزهای بهداشتی، جلب اعتماد بیماران، پر شدن نوبت‌های خالی، تامین استریل",
            openingPitchHook = "«وقت‌بخیر خانم دکتر، ما به بیش از ۴۰ کلینیک تخصصی کمک کردیم تا فرآیند تامین لوازم مصرفی رو بدون تاخیر و با تاییدیه وزارت بهداشت صفر تا صد مدیریت کنند...»",
            bestLeadSources = listOf("سامانه نظام پزشکی", "انجمن‌های تخصصی پزشکی", "بانک اطلاعات کلینیک‌های زیبایی و درمانی"),
            recommendedSampleCompanyName = "مرکز جراحی و کلینیک تابان",
            recommendedContactName = "دکتر وحید رحمانی (مدیر کلینیک)"
        ),
        LeadSuggestion(
            industry = "ساختمان، انبوه‌سازان و املاک",
            targetPersona = "مجری طرح، مدیر پروژه ساختمانی، مهندس ناظر یا سرمایه‌گذار",
            corePainPoint = "افزایش هزینه مصالح، عدم تاخیر در پایان کار، استانداردهای آتش‌نشانی و ایمنی، فروش واحدهای آماده",
            openingPitchHook = "«سلام مهندس، پروژه‌تون رو در مرحله نازک‌کاری دیدم؛ ما شرایط ویژه‌ای برای مصالح با تحویل مستقیم از کارخانه داریم که باعث صرفه‌جویی ۱۵ درصدی در بودجه هر متر مربع میشه...»",
            bestLeadSources = listOf("سایت‌های صدور پروانه شهرداری", "نمایشگاه صنعت ساختمان", "اتحادیه صنف انبوه‌سازان"),
            recommendedSampleCompanyName = "گروه توسعه ابنیه سازان نگین",
            recommendedContactName = "مهندس فرهاد صدر (مجری پروژه)"
        ),
        LeadSuggestion(
            industry = "خدمات مالی، بیمه و مشاوره",
            targetPersona = "مدیر مالی، حسابدار ارشد، مدیر منابع انسانی",
            corePainPoint = "ریسک‌های مالیاتی، هزینه‌های بیمه پرسنل، کاهش ریسک حسابرسی و جریمه‌ها",
            openingPitchHook = "«سلام جناب، با توجه به بخشنامه‌های جدید مالیاتی، ما راهکاری برای شرکت‌ها پیاده کردیم که مالیات عملکردشون رو کاملاً قانونی به حداقل برسونه...»",
            bestLeadSources = listOf("انجمن حسابداران خبره", "شبکه کارشناسان مالیاتی", "رویدادهای کارآفرینی"),
            recommendedSampleCompanyName = "مشاوران مالی و مالیاتی تراز نوین",
            recommendedContactName = "خانم پورمند (مدیر مالی)"
        )
    )

    /**
     * Performs analysis of the marketer's conversation or call notes.
     * Uses Gemini API when available, otherwise falls back to smart rule-based sales critique!
     */
    suspend fun analyzeCallText(callText: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val prompt = """
                    شما یک مربی ارشد و کارشناس خبره آموزش فروش و بازاریابی تلفنی B2B و B2C هستید.
                    یک بازاریاب متن زیر را از نتیجه تماس، مکالمه یا بهانه مشتری برای شما فرستاده است:
                    «$callText»

                    لطفاً یک تحلیل جامع، دقیق، کاربردی و روان به زبان فارسی در قالب این ۴ بخش ارائه بده:
                    ۱. 🎯 تحلیل وضعیت و ایرادگیری: (نقاط ضعف مکالمه، فرصت‌های از دست رفته، آیا مشتری بهانه آورد یا مشکل واقعی داشت؟)
                    ۲. 💡 روانشناسی اعتراض مشتری: (پشت این حرف مشتری واقعاً چه ترسی یا دغدغه‌ای نهفته است؟)
                    ۳. 🗣️ سناریو و متن پاسخ طلایی: (دقیقاً کلمه به کلمه به بازاریاب بگو در تماس بعدی یا همان لحظه چه جمله‌ای بگوید)
                    ۴. 🚀 تکنیک پیگیری بعدی: (چند روز بعد تماس بگیرد و با چه بهانه مثبتی مکالمه را شروع کند تا معامله بسته شود؟)
                """.trimIndent()

                val result = callGeminiRest(apiKey, prompt)
                if (result.isNotBlank()) {
                    return@withContext result
                }
            } catch (e: Exception) {
                // fallback to local rule-based coaching
            }
        }

        // Local Smart Sales Coaching Engine (Offline fallback)
        return@withContext generateLocalCoaching(callText)
    }

    private fun generateLocalCoaching(text: String): String {
        val lower = text.lowercase()
        val hasPrice = lower.contains("گرون") || lower.contains("قیمت") || lower.contains("تخفیف") || lower.contains("هزینه")
        val hasTime = lower.contains("وقت") || lower.contains("الان نه") || lower.contains("سرم شلوغه") || lower.contains("بعداً") || lower.contains("بعدا")
        val hasCompetitor = lower.contains("رقیب") || lower.contains("شرکت دیگه") || lower.contains("جای دیگه") || lower.contains("تامین")
        val hasEmail = lower.contains("ایمیل") || lower.contains("واتساپ") || lower.contains("کاتالوگ") || lower.contains("پروپوزال") || lower.contains("ارسال")
        val hasBudget = lower.contains("بودجه") || lower.contains("پول") || lower.contains("اوضاع")

        val sb = StringBuilder()
        sb.append("📋 نتیجه تحلیل و ایرادگیری هوشمند مربی فروش:\n\n")

        sb.append("۱. 🎯 نقد مکالمه و شناسایی ایراد:\n")
        when {
            hasPrice -> sb.append("• ورود زودهنگام به بحث قیمت: احتمالاً قبل از اینکه ارزش و بازگشت سرمایه را در ذهن مشتری جا بیندازید، قیمت را مطرح کرده‌اید یا در برابر تخفیف مقاومت ضعیفی نشان داده‌اید.\n")
            hasTime -> sb.append("• کمبود قلاب جذاب (Hook): در ۱۰ ثانیه ابتدایی توجه مخاطب جلب نشده و او مکالمه را وقت‌گیر تلقی کرده است.\n")
            hasCompetitor -> sb.append("• عدم تمرکز بر تمایز: مشتری نیازی به تعویض نمی‌بیند چون ارزش افزوده انحصاری شما برایش روشن نشده است.\n")
            hasEmail -> sb.append("• فرار محترمانه مشتری: ارسال کاتالوگ معمولاً روشی برای تمام کردن تماس است بدون اینکه تعهدی ایجاد شود.\n")
            hasBudget -> sb.append("• تبدیل محصول به هزینه به جای راهکار سودآوری: مشتری احساس می‌کند این خرید یک خرج اضافه در بحران است.\n")
            else -> sb.append("• نیاز به ایجاد فوریت و سوالات پرس‌وجوگرایانه: مکالمه حالت یک‌طرفه داشته و به نیازهای عمیق مشتری نپرداخته است.\n")
        }

        sb.append("\n۲. 💡 روانشناسی مشتری در این مکالمه:\n")
        when {
            hasPrice -> sb.append("• مشتری نمی‌گوید پول ندارم؛ او می‌گوید: «هنوز متقاعد نشدم که محصول شما اینقدر ارزش دارد!»\n")
            hasTime -> sb.append("• مشتری اولویت‌های کاری زیادی دارد؛ تماس شما باید احساس شود که کار او را سبک‌تر و سودآورتر می‌کند.\n")
            hasCompetitor -> sb.append("• مشتری از ریسک جابه‌جایی و احتمال خطا در تحویل می‌ترسد؛ به او اطمینان خاطر و گارانتی بدهید.\n")
            else -> sb.append("• مشتری به دنبال یک راه‌حل بی‌دردسر است و نیاز دارد حس کند شما متخصص حوزه او هستید نه صرفاً یک فروشنده تلفنی.\n")
        }

        sb.append("\n۳. 🗣️ اسکریپت پیشنهادی برای تماس بعدی:\n")
        sb.append("«سلام مهندس، پیرو صحبت قبلی‌مون، روی چالش شما فکر کردم و یک راه‌حل ویژه طراحی کردیم که ریسک اولیه شما رو صفر می‌کنه. مایلید در ۲ دقیقه خلاصه اون رو بهتون بگم؟»\n")

        sb.append("\n۴. 🚀 اقدام توصیه‌شده برای پیگیری:\n")
        sb.append("• حداکثر تا ۲ الی ۳ روز آینده یک یادآور پیگیری تنظیم کنید.\n")
        sb.append("• از مشتری نپرسید «تصمیمتون چی شد؟»؛ بلکه یک خبر، دستاورد جدید یا نمونه کار موفق از مشتریان مشابه همان صنعت برای او بفرستید.")

        return sb.toString()
    }

    private fun callGeminiRest(apiKey: String, prompt: String): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
        val requestJson = JSONObject().apply {
            val contentsArr = JSONArray().apply {
                val item = JSONObject().apply {
                    val parts = JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                    }
                    put("parts", parts)
                }
                put(item)
            }
            put("contents", contentsArr)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = requestJson.toString().toRequestBody(mediaType)
        val request = Request.Builder().url(url).post(body).build()

        val response = okHttpClient.newCall(request).execute()
        val resBody = response.body?.string() ?: return ""
        if (!response.isSuccessful) return ""

        val json = JSONObject(resBody)
        val candidates = json.optJSONArray("candidates") ?: return ""
        if (candidates.length() == 0) return ""
        val candidate = candidates.getJSONObject(0)
        val content = candidate.optJSONObject("content") ?: return ""
        val parts = content.optJSONArray("parts") ?: return ""
        if (parts.length() == 0) return ""
        return parts.getJSONObject(0).optString("text", "")
    }
}
