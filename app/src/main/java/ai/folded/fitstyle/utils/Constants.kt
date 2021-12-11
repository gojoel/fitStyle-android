package ai.folded.fitstyle.utils

// S3
const val BUCKET_PUBLIC_PREFIX = "public/"
const val BUCKET_PRIVATE_PREFIX = "private/"
const val BUCKET_REQUESTS = "requests/"
const val STYLE_IMAGES_PATH = "style_images/"
const val STYLED_IMAGE_NAME = "styled.jpg"
const val PREVIEW_IMAGE_NAME = "preview.jpg"

// Stripe
const val MERCHANT = "Folded AI"
const val CURRENCY = "usd"
const val COUNTRY_CODE = "US"

const val DATABASE_NAME = "fitstyle-db"
const val CACHE_DIR_CHILD = "images"
const val PREF_KEY_USER_ID = "user_id"
const val CONTACT_EMAIL = "foldedai@gmail.com"

const val URL_FETCHER_TIMEOUT : Int = 15000
const val URL_EXPIRATION_SEC : Long = 10800 // 3 hrs
const val TRANSFER_RETRIES = 10L
const val MAX_IMAGE_SIZE = 1500

const val ERROR_TYPE_STYLE_TRANSFER : Int = 0

const val STYLED_IMG_VIEW_SRC_DEFAULT = 0
const val STYLED_IMG_VIEW_SRC_TRANSFER = 1

// shared preferences
const val PREF_COMPLETED_TUTORIAL = "pref_completed_tutorial"