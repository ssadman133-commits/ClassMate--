package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.data.CoverPageData
import com.example.data.CoverPageTemplate
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * Utility for exporting Assignment Cover Pages to:
 * 1. Razor-sharp A4 Vector PDF (Print Ready: 595 x 842 pt)
 * 2. High Resolution HD Image (1190 x 1684 px @ 300 DPI)
 *
 * Implements 6 radically distinct, publication-grade academic layout designs.
 */
object CoverPageExporter {

    // Standard A4 dimensions in typographic points (72 DPI standard for PDF)
    const val A4_WIDTH_PTS = 595
    const val A4_HEIGHT_PTS = 842

    // 300 DPI High-Resolution Image dimensions
    const val HD_IMG_WIDTH = 1190
    const val HD_IMG_HEIGHT = 1684

    /**
     * Generates a temporary PDF file for sharing or preview.
     */
    fun generatePdf(context: Context, data: CoverPageData): Uri? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(A4_WIDTH_PTS, A4_HEIGHT_PTS, 1).create()
        val page = document.startPage(pageInfo)

        val canvas = page.canvas
        drawCoverPage(context, canvas, A4_WIDTH_PTS.toFloat(), A4_HEIGHT_PTS.toFloat(), data, 1.0f)
        document.finishPage(page)

        return try {
            val fileName = "CoverPage_${System.currentTimeMillis()}.pdf"
            val outputDir = File(context.cacheDir, "cover_pages").apply { mkdirs() }
            val outputFile = File(outputDir, fileName)

            FileOutputStream(outputFile).use { out ->
                document.writeTo(out)
            }
            document.close()

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                outputFile
            )
        } catch (_: Exception) {
            document.close()
            null
        }
    }

    /**
     * Saves print-ready A4 PDF directly to Public Downloads folder.
     */
    fun savePdfToDownloads(context: Context, data: CoverPageData): Uri? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(A4_WIDTH_PTS, A4_HEIGHT_PTS, 1).create()
        val page = document.startPage(pageInfo)

        val canvas = page.canvas
        drawCoverPage(context, canvas, A4_WIDTH_PTS.toFloat(), A4_HEIGHT_PTS.toFloat(), data, 1.0f)
        document.finishPage(page)

        val sanitizedTitle = data.assignmentTitle.replace("[^a-zA-Z0-9_-]".toRegex(), "_").take(30)
            .ifBlank { "Assignment" }
        val fileName = "${sanitizedTitle}_CoverPage_${System.currentTimeMillis()}.pdf"

        var outputStream: OutputStream? = null
        var resultUri: Uri? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/ClassMate")
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    outputStream = resolver.openOutputStream(uri)
                    resultUri = uri
                }
            } else {
                @Suppress("DEPRECATION")
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val targetDir = File(downloadsDir, "ClassMate").apply { mkdirs() }
                val targetFile = File(targetDir, fileName)
                outputStream = FileOutputStream(targetFile)
                resultUri = try {
                    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", targetFile)
                } catch (_: Exception) {
                    Uri.fromFile(targetFile)
                }
            }

            outputStream?.use { out ->
                document.writeTo(out)
            }
            document.close()
            return resultUri
        } catch (_: Exception) {
            document.close()
            return null
        }
    }

    /**
     * Generates a high-res bitmap for gallery saving.
     */
    fun generateBitmap(context: Context, data: CoverPageData): Bitmap {
        val bitmap = Bitmap.createBitmap(HD_IMG_WIDTH, HD_IMG_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val scale = HD_IMG_WIDTH.toFloat() / A4_WIDTH_PTS.toFloat()
        drawCoverPage(context, canvas, HD_IMG_WIDTH.toFloat(), HD_IMG_HEIGHT.toFloat(), data, scale)
        return bitmap
    }

    /**
     * Saves high-resolution PNG image directly into Pictures/ClassMate gallery.
     */
    fun saveImageToGallery(context: Context, data: CoverPageData): Uri? {
        val bitmap = generateBitmap(context, data)
        val sanitizedTitle = data.assignmentTitle.replace("[^a-zA-Z0-9_-]".toRegex(), "_").take(30)
            .ifBlank { "Assignment" }
        val fileName = "${sanitizedTitle}_CoverPage_${System.currentTimeMillis()}.png"

        var outputStream: OutputStream? = null
        var resultUri: Uri? = null

        try {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ClassMate")
                }
            }
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                outputStream = resolver.openOutputStream(uri)
                resultUri = uri
            }

            outputStream?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            return resultUri
        } catch (_: Exception) {
            return null
        }
    }

    fun shareFile(context: Context, uri: Uri, mimeType: String, title: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Share Cover Page")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun openFile(context: Context, uri: Uri, mimeType: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            val chooser = Intent.createChooser(intent, "Open Cover Page")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        }
    }

    /**
     * Core Drawing Engine: Dispatches to 6 radically distinct layout architectures.
     */
    fun drawCoverPage(
        context: Context,
        canvas: Canvas,
        width: Float,
        height: Float,
        data: CoverPageData,
        scale: Float
    ) {
        val template = CoverPageTemplate.fromId(data.templateId)

        // 1. Draw Page Background
        val bgPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width, height, bgPaint)

        // Decode Logo if provided
        val logoBitmap = decodeUserLogo(context, data.logoUri)

        // 2. Draw Distinct Architecture
        when (template) {
            CoverPageTemplate.MODERN_MINIMAL -> drawTemplate1ModernStripe(canvas, width, height, data, logoBitmap, scale)
            CoverPageTemplate.CORPORATE_RIBBON -> drawTemplate2RoyalBanner(canvas, width, height, data, logoBitmap, scale)
            CoverPageTemplate.CLASSIC_FORMAL -> drawTemplate3OxfordHeritage(canvas, width, height, data, logoBitmap, scale)
            CoverPageTemplate.TECH_GRID -> drawTemplate4StemMatrix(canvas, width, height, data, logoBitmap, scale)
            CoverPageTemplate.EMERALD_ACADEMIC -> drawTemplate5EmeraldScholar(canvas, width, height, data, logoBitmap, scale)
            CoverPageTemplate.CRIMSON_PRESTIGE -> drawTemplate6CrimsonMagazine(canvas, width, height, data, logoBitmap, scale)
        }
    }

    // =========================================================================
    // TEMPLATE 1: MODERN SAPPHIRE STRIPE (Left Accent Bar + Stacked Cards)
    // =========================================================================
    private fun drawTemplate1ModernStripe(
        canvas: Canvas,
        width: Float,
        height: Float,
        data: CoverPageData,
        logo: Bitmap?,
        scale: Float
    ) {
        val sapphire = 0xFF2563EB.toInt()
        val sky = 0xFF0EA5E9.toInt()
        val textDark = 0xFF0F172A.toInt()

        // 1. Vertical Left Stripe (16pt wide)
        val stripePaint = Paint().apply { color = sapphire; style = Paint.Style.FILL }
        canvas.drawRect(0f, 0f, 16f * scale, height, stripePaint)

        val contentLeft = 45f * scale
        val contentRight = width - 35f * scale

        // 2. Header: University Left, Logo Right
        val logoSize = 65f * scale
        val logoRect = RectF(contentRight - logoSize, 45f * scale, contentRight, 45f * scale + logoSize)
        drawLogoOrFallback(canvas, logo, logoRect, sapphire, scale)

        val uniPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textDark
            textSize = 19f * scale
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText(data.universityName.uppercase(), contentLeft, 65f * scale, uniPaint)

        if (data.department.isNotBlank()) {
            val deptPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = sapphire
                textSize = 11.5f * scale
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                textAlign = Paint.Align.LEFT
            }
            canvas.drawText(data.department, contentLeft, 85f * scale, deptPaint)
        }

        // 3. Center Title Block (In a sleek framed card)
        val titleBoxTop = 270f * scale
        val titleBoxHeight = 130f * scale
        val cardRect = RectF(contentLeft, titleBoxTop, contentRight, titleBoxTop + titleBoxHeight)

        val cardBgPaint = Paint().apply { color = 0xFFF8FAFC.toInt(); style = Paint.Style.FILL }
        val cardBorderPaint = Paint().apply {
            color = sapphire
            style = Paint.Style.STROKE
            strokeWidth = 2f * scale
        }
        canvas.drawRoundRect(cardRect, 14f * scale, 14f * scale, cardBgPaint)
        canvas.drawRoundRect(cardRect, 14f * scale, 14f * scale, cardBorderPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textDark
            textSize = 18f * scale
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val titleLines = wrapText(data.assignmentTitle.ifBlank { "Assignment Title" }, titlePaint, (contentRight - contentLeft) - 30f * scale)
        val titleStartY = titleBoxTop + 45f * scale
        titleLines.forEachIndexed { i, line ->
            canvas.drawText(line, (contentLeft + contentRight) / 2f, titleStartY + (i * 24f * scale), titlePaint)
        }

        // Course Pill below Title
        val course = "${data.courseCode} : ${data.courseTitle}".trim().trim(':').trim()
        if (course.isNotBlank()) {
            val coursePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = sapphire
                textSize = 11f * scale
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            val pillWidth = coursePaint.measureText(course) + 30f * scale
            val pillRect = RectF(
                (width - pillWidth) / 2f,
                titleBoxTop + titleBoxHeight + 15f * scale,
                (width + pillWidth) / 2f,
                titleBoxTop + titleBoxHeight + 42f * scale
            )
            val pillBg = Paint().apply { color = 0xFFEFF6FF.toInt(); style = Paint.Style.FILL }
            canvas.drawRoundRect(pillRect, 14f * scale, 14f * scale, pillBg)
            canvas.drawText(course, width / 2f, titleBoxTop + titleBoxHeight + 33f * scale, coursePaint)
        }

        // 4. Bottom Stacked Profile Cards
        val card1Top = 500f * scale
        val card1Height = 85f * scale
        drawModernStackCard(canvas, contentLeft, card1Top, contentRight, card1Top + card1Height, sapphire, "PREPARED FOR", listOf(
            data.facultyName,
            data.facultyDesignation,
            data.facultyDepartment
        ), scale)

        val card2Top = 605f * scale
        val card2Height = 95f * scale
        drawModernStackCard(canvas, contentLeft, card2Top, contentRight, card2Top + card2Height, sky, "PREPARED BY", listOf(
            data.studentName,
            "Student ID: ${data.studentId}",
            listOf(data.department, data.batchSection).filter { it.isNotBlank() }.joinToString(" • ")
        ), scale)

        // 5. Footer Date
        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF64748B.toInt()
            textSize = 10f * scale
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("Submission Date: ${data.submissionDate}", contentRight, height - 35f * scale, datePaint)
    }

    // =========================================================================
    // TEMPLATE 2: ROYAL EXECUTIVE BANNER (Top 24% Navy Banner + Centered Emblem)
    // =========================================================================
    private fun drawTemplate2RoyalBanner(
        canvas: Canvas,
        width: Float,
        height: Float,
        data: CoverPageData,
        logo: Bitmap?,
        scale: Float
    ) {
        val royalIndigo = 0xFF1E1B4B.toInt()
        val accentViolet = 0xFF6366F1.toInt()
        val textDark = 0xFF0F172A.toInt()

        // Top 24% Solid Navy Banner
        val headerHeight = 200f * scale
        val headerPaint = Paint().apply { color = royalIndigo; style = Paint.Style.FILL }
        canvas.drawRect(0f, 0f, width, headerHeight, headerPaint)

        // University Name in Header
        val uniPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 20f * scale
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(data.universityName.uppercase(), width / 2f, 75f * scale, uniPaint)

        if (data.department.isNotBlank()) {
            val deptPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFC7D2FE.toInt()
                textSize = 12f * scale
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(data.department, width / 2f, 105f * scale, deptPaint)
        }

        // Circular Emblem intersecting banner boundary
        val emblemSize = 85f * scale
        val emblemRect = RectF((width - emblemSize) / 2f, headerHeight - emblemSize / 2f, (width + emblemSize) / 2f, headerHeight + emblemSize / 2f)
        val whiteBg = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
        canvas.drawOval(emblemRect, whiteBg)
        drawLogoOrFallback(canvas, logo, emblemRect, accentViolet, scale)

        // Center Title Block
        val titleY = 360f * scale
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textDark
            textSize = 21f * scale
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val titleLines = wrapText(data.assignmentTitle.ifBlank { "Assignment Title" }, titlePaint, width - 100f * scale)
        titleLines.forEachIndexed { i, line ->
            canvas.drawText(line, width / 2f, titleY + (i * 26f * scale), titlePaint)
        }

        // Horizontal accent line
        val linePaint = Paint().apply { color = accentViolet; strokeWidth = 2.5f * scale }
        val lineY = titleY + (titleLines.size * 26f * scale) + 10f * scale
        canvas.drawLine(width / 2f - 40f * scale, lineY, width / 2f + 40f * scale, lineY, linePaint)

        // Course subtitle
        val course = "${data.courseCode} • ${data.courseTitle}".trim().trim('•').trim()
        if (course.isNotBlank()) {
            val coursePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF4B5563.toInt()
                textSize = 12f * scale
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(course, width / 2f, lineY + 25f * scale, coursePaint)
        }

        // Two Floating Cards Side-by-Side
        val cardWidth = (width - 80f * scale) / 2f
        val cardTop = 550f * scale
        val cardHeight = 140f * scale

        // Left Card: Faculty
        drawFloatingCard(canvas, 30f * scale, cardTop, 30f * scale + cardWidth, cardTop + cardHeight, accentViolet, "SUPERVISOR", listOf(
            data.facultyName,
            data.facultyDesignation,
            data.facultyDepartment
        ), scale)

        // Right Card: Student
        drawFloatingCard(canvas, width - 30f * scale - cardWidth, cardTop, width - 30f * scale, cardTop + cardHeight, royalIndigo, "STUDENT", listOf(
            data.studentName,
            "ID: ${data.studentId}",
            data.batchSection
        ), scale)

        // Footer Date and Bottom Stripe
        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF64748B.toInt()
            textSize = 11f * scale
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Date of Submission: ${data.submissionDate}", width / 2f, height - 40f * scale, datePaint)

        val bottomPaint = Paint().apply { color = royalIndigo; style = Paint.Style.FILL }
        canvas.drawRect(0f, height - 12f * scale, width, height, bottomPaint)
    }

    // =========================================================================
    // TEMPLATE 3: OXFORD HERITAGE THESIS (Double Border + 100% Serif + Columns)
    // =========================================================================
    private fun drawTemplate3OxfordHeritage(
        canvas: Canvas,
        width: Float,
        height: Float,
        data: CoverPageData,
        logo: Bitmap?,
        scale: Float
    ) {
        val goldColor = 0xFFD97706.toInt()
        val navyColor = 0xFF1E3A8A.toInt()
        val textDark = 0xFF0F172A.toInt()

        // Double Outer Border (Warm Gold outer, Navy inner)
        val outerBorder = Paint().apply {
            color = goldColor
            style = Paint.Style.STROKE
            strokeWidth = 2.5f * scale
        }
        val marginOuter = 20f * scale
        canvas.drawRect(marginOuter, marginOuter, width - marginOuter, height - marginOuter, outerBorder)

        val innerBorder = Paint().apply {
            color = navyColor
            style = Paint.Style.STROKE
            strokeWidth = 1f * scale
        }
        val marginInner = 25f * scale
        canvas.drawRect(marginInner, marginInner, width - marginInner, height - marginInner, innerBorder)

        var currentY = 70f * scale

        // Institution Name (Serif Bold)
        val uniPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = navyColor
            textSize = 21f * scale
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(data.universityName.uppercase(), width / 2f, currentY, uniPaint)
        currentY += 22f * scale

        if (data.department.isNotBlank()) {
            val deptPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF475569.toInt()
                textSize = 12f * scale
                typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(data.department, width / 2f, currentY, deptPaint)
            currentY += 15f * scale
        }

        // Ornamental Divider
        val ornamentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = goldColor
            textSize = 13f * scale
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("─── ◆ ───", width / 2f, currentY + 10f * scale, ornamentPaint)
        currentY += 30f * scale

        // Centered Emblem
        val logoSize = 75f * scale
        val logoRect = RectF((width - logoSize) / 2f, currentY, (width + logoSize) / 2f, currentY + logoSize)
        drawLogoOrFallback(canvas, logo, logoRect, goldColor, scale)
        currentY += logoSize + 60f * scale

        // Assignment Title (Large Serif Uppercase)
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textDark
            textSize = 21f * scale
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val titleLines = wrapText(data.assignmentTitle.ifBlank { "Assignment Title" }.uppercase(), titlePaint, width - 120f * scale)
        titleLines.forEachIndexed { i, line ->
            canvas.drawText(line, width / 2f, currentY + (i * 28f * scale), titlePaint)
        }
        currentY += (titleLines.size * 28f * scale) + 15f * scale

        canvas.drawText("─── ◆ ───", width / 2f, currentY, ornamentPaint)
        currentY += 25f * scale

        val course = "${data.courseCode} - ${data.courseTitle}".trim().trim('-').trim()
        if (course.isNotBlank()) {
            val coursePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = navyColor
                textSize = 13f * scale
                typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(course, width / 2f, currentY, coursePaint)
        }

        // Traditional Thesis Signature Columns (No cards, pure classic typesetting)
        val colY = 560f * scale
        val leftX = 55f * scale
        val rightX = width - 55f * scale

        // Left: Submitted To
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = navyColor
            textSize = 12f * scale
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText("Submitted To:", leftX, colY, headerPaint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textDark
            textSize = 12.5f * scale
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText(data.facultyName, leftX, colY + 22f * scale, textPaint)

        textPaint.typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        textPaint.color = 0xFF475569.toInt()
        textPaint.textSize = 11f * scale
        canvas.drawText(data.facultyDesignation, leftX, colY + 40f * scale, textPaint)
        canvas.drawText(data.facultyDepartment, leftX, colY + 58f * scale, textPaint)

        // Right: Submitted By
        headerPaint.color = goldColor
        headerPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Submitted By:", rightX, colY, headerPaint)

        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.color = textDark
        textPaint.textSize = 12.5f * scale
        textPaint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        canvas.drawText(data.studentName, rightX, colY + 22f * scale, textPaint)

        textPaint.typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        textPaint.color = 0xFF475569.toInt()
        textPaint.textSize = 11f * scale
        canvas.drawText("ID: ${data.studentId}", rightX, colY + 40f * scale, textPaint)
        canvas.drawText(data.batchSection, rightX, colY + 58f * scale, textPaint)

        // Footer Date
        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = navyColor
            textSize = 11.5f * scale
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Date of Submission: ${data.submissionDate}", width / 2f, height - 50f * scale, datePaint)
    }

    // =========================================================================
    // TEMPLATE 4: STEM CYBER MATRIX (Technical Grid Boxes + Monospace Styling)
    // =========================================================================
    private fun drawTemplate4StemMatrix(
        canvas: Canvas,
        width: Float,
        height: Float,
        data: CoverPageData,
        logo: Bitmap?,
        scale: Float
    ) {
        val cyanColor = 0xFF0284C7.toInt()
        val tealColor = 0xFF0D9488.toInt()
        val darkSlate = 0xFF0F172A.toInt()

        // Top Dark Monospace Banner
        val topBannerPaint = Paint().apply { color = darkSlate; style = Paint.Style.FILL }
        canvas.drawRect(0f, 0f, width, 32f * scale, topBannerPaint)

        val bannerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF38BDF8.toInt()
            textSize = 11f * scale
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText("// ACADEMIC LAB MANUAL & PROJECT REPORT //", 30f * scale, 22f * scale, bannerTextPaint)

        // Header: Left Uni Info, Right Logo in square crop box
        val headerLeft = 35f * scale
        val headerRight = width - 35f * scale
        val logoBoxSize = 60f * scale
        val logoRect = RectF(headerRight - logoBoxSize, 55f * scale, headerRight, 55f * scale + logoBoxSize)

        val techBorder = Paint().apply { color = cyanColor; style = Paint.Style.STROKE; strokeWidth = 1.5f * scale }
        canvas.drawRect(logoRect, techBorder)
        drawLogoOrFallback(canvas, logo, logoRect, cyanColor, scale)

        val uniPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = darkSlate
            textSize = 17f * scale
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText(data.universityName.uppercase(), headerLeft, 75f * scale, uniPaint)

        val deptPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = cyanColor
            textSize = 11f * scale
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText("DEPT: ${data.department.uppercase()}", headerLeft, 95f * scale, deptPaint)

        // Technical Box for Title
        val titleBoxTop = 200f * scale
        val titleBoxHeight = 150f * scale
        val titleBoxRect = RectF(headerLeft, titleBoxTop, headerRight, titleBoxTop + titleBoxHeight)

        canvas.drawRect(titleBoxRect, techBorder)

        // Tab header
        val tabPaint = Paint().apply { color = cyanColor; style = Paint.Style.FILL }
        canvas.drawRect(headerLeft, titleBoxTop, headerRight, titleBoxTop + 24f * scale, tabPaint)

        val tabTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 10f * scale
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }
        canvas.drawText("SPECIFICATION: PROJECT TITLE", headerLeft + 12f * scale, titleBoxTop + 16f * scale, tabTextPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = darkSlate
            textSize = 18f * scale
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }
        val titleLines = wrapText(data.assignmentTitle.ifBlank { "Engineering Project Title" }, titlePaint, (headerRight - headerLeft) - 30f * scale)
        titleLines.forEachIndexed { i, line ->
            canvas.drawText(line, headerLeft + 15f * scale, titleBoxTop + 55f * scale + (i * 24f * scale), titlePaint)
        }

        val codePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = tealColor
            textSize = 11f * scale
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }
        val courseCodeStr = "[ CODE: ${data.courseCode.ifBlank { "N/A" }} ] [ UNIT: ${data.courseTitle.ifBlank { "N/A" }} ]"
        canvas.drawText(courseCodeStr, headerLeft + 15f * scale, titleBoxTop + titleBoxHeight - 18f * scale, codePaint)

        // Bottom Modular Data Grid Tables
        val gridTop = 450f * scale
        val gridWidth = (headerRight - headerLeft - 20f * scale) / 2f
        val gridHeight = 160f * scale

        // Box 1: Evaluator
        drawTechDataBox(canvas, headerLeft, gridTop, headerLeft + gridWidth, gridTop + gridHeight, cyanColor, "[01] EVALUATOR", listOf(
            "NAME: ${data.facultyName}",
            "RANK: ${data.facultyDesignation}",
            "DEPT: ${data.facultyDepartment}"
        ), scale)

        // Box 2: Candidate
        drawTechDataBox(canvas, headerRight - gridWidth, gridTop, headerRight, gridTop + gridHeight, tealColor, "[02] CANDIDATE", listOf(
            "NAME: ${data.studentName}",
            "ID:   ${data.studentId}",
            "SEC:  ${data.batchSection}"
        ), scale)

        // Footer Timestamp
        val footerPaint = Paint().apply { color = 0xFFF1F5F9.toInt(); style = Paint.Style.FILL }
        canvas.drawRect(0f, height - 40f * scale, width, height, footerPaint)

        val timePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF475569.toInt()
            textSize = 10.5f * scale
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("TIMESTAMP // ${data.submissionDate.uppercase()}", width / 2f, height - 16f * scale, timePaint)
    }

    // =========================================================================
    // TEMPLATE 5: EMERALD SCHOLAR (Geometric Corner Accents + Profile Cards)
    // =========================================================================
    private fun drawTemplate5EmeraldScholar(
        canvas: Canvas,
        width: Float,
        height: Float,
        data: CoverPageData,
        logo: Bitmap?,
        scale: Float
    ) {
        val emerald = 0xFF059669.toInt()
        val mint = 0xFF10B981.toInt()
        val gold = 0xFFF59E0B.toInt()
        val textDark = 0xFF0F172A.toInt()

        // Top Geometric Strip
        val topStripe = Paint().apply { color = emerald; style = Paint.Style.FILL }
        canvas.drawRect(0f, 0f, width * 0.7f, 10f * scale, topStripe)
        topStripe.color = gold
        canvas.drawRect(width * 0.7f, 0f, width, 10f * scale, topStripe)

        // Header: Centered Logo & University
        var currentY = 55f * scale
        val logoSize = 75f * scale
        val logoRect = RectF((width - logoSize) / 2f, currentY, (width + logoSize) / 2f, currentY + logoSize)
        drawLogoOrFallback(canvas, logo, logoRect, emerald, scale)
        currentY += logoSize + 25f * scale

        val uniPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = emerald
            textSize = 20f * scale
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(data.universityName.uppercase(), width / 2f, currentY, uniPaint)
        currentY += 20f * scale

        if (data.department.isNotBlank()) {
            val deptPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF047857.toInt()
                textSize = 12f * scale
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(data.department, width / 2f, currentY, deptPaint)
        }

        // Title in tinted emerald card with solid 6dp left border
        val titleCardTop = 270f * scale
        val titleCardHeight = 130f * scale
        val cardRect = RectF(35f * scale, titleCardTop, width - 35f * scale, titleCardTop + titleCardHeight)

        val cardBg = Paint().apply { color = 0xFFECFDF5.toInt(); style = Paint.Style.FILL }
        canvas.drawRoundRect(cardRect, 10f * scale, 10f * scale, cardBg)

        // Left solid bar
        val leftBar = Paint().apply { color = emerald; style = Paint.Style.FILL }
        val barRect = RectF(35f * scale, titleCardTop, 43f * scale, titleCardTop + titleCardHeight)
        canvas.drawRoundRect(barRect, 6f * scale, 6f * scale, leftBar)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF064E3B.toInt()
            textSize = 19f * scale
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val titleLines = wrapText(data.assignmentTitle.ifBlank { "Assignment Topic" }, titlePaint, width - 130f * scale)
        titleLines.forEachIndexed { i, line ->
            canvas.drawText(line, width / 2f, titleCardTop + 45f * scale + (i * 26f * scale), titlePaint)
        }

        val course = "${data.courseCode} : ${data.courseTitle}".trim().trim(':').trim()
        if (course.isNotBlank()) {
            val coursePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = emerald
                textSize = 11.5f * scale
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(course, width / 2f, titleCardTop + titleCardHeight - 20f * scale, coursePaint)
        }

        // Bottom Profiles (Faculty & Student)
        val p1Top = 480f * scale
        val p1Height = 85f * scale
        drawProfileCard(canvas, 35f * scale, p1Top, width - 35f * scale, p1Top + p1Height, emerald, "SUPERVISED BY", data.facultyName, "${data.facultyDesignation}, ${data.facultyDepartment}".trim().trim(',').trim(), scale)

        val p2Top = 580f * scale
        val p2Height = 85f * scale
        drawProfileCard(canvas, 35f * scale, p2Top, width - 35f * scale, p2Top + p2Height, mint, "SUBMITTED BY", data.studentName, "ID: ${data.studentId} • ${data.batchSection}", scale)

        // Date Pill
        val dateText = "Submission Date: ${data.submissionDate}"
        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF065F46.toInt()
            textSize = 11f * scale
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val dateWidth = datePaint.measureText(dateText) + 30f * scale
        val dateRect = RectF((width - dateWidth) / 2f, height - 65f * scale, (width + dateWidth) / 2f, height - 35f * scale)
        val dateBg = Paint().apply { color = 0xFFD1FAE5.toInt(); style = Paint.Style.FILL }
        canvas.drawRoundRect(dateRect, 15f * scale, 15f * scale, dateBg)
        canvas.drawText(dateText, width / 2f, height - 46f * scale, datePaint)
    }

    // =========================================================================
    // TEMPLATE 6: CRIMSON MAGAZINE SPLIT (28% Vertical Column Split + 72% White)
    // =========================================================================
    private fun drawTemplate6CrimsonMagazine(
        canvas: Canvas,
        width: Float,
        height: Float,
        data: CoverPageData,
        logo: Bitmap?,
        scale: Float
    ) {
        val crimson = 0xFFDC2626.toInt()
        val textDark = 0xFF0F172A.toInt()

        // 1. Left 28% Crimson Vertical Column
        val colWidth = width * 0.28f
        val colPaint = Paint().apply { color = crimson; style = Paint.Style.FILL }
        canvas.drawRect(0f, 0f, colWidth, height, colPaint)

        // Logo inside white circle at top of column
        val logoSize = 65f * scale
        val logoRect = RectF((colWidth - logoSize) / 2f, 50f * scale, (colWidth + logoSize) / 2f, 50f * scale + logoSize)
        val whiteCircle = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
        canvas.drawOval(logoRect, whiteCircle)
        drawLogoOrFallback(canvas, logo, logoRect, crimson, scale)

        // University Name in White inside column
        val uniPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 13.5f * scale
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val uniLines = wrapText(data.universityName.uppercase(), uniPaint, colWidth - 20f * scale)
        uniLines.forEachIndexed { i, line ->
            canvas.drawText(line, colWidth / 2f, 140f * scale + (i * 18f * scale), uniPaint)
        }

        // Date at bottom of column
        val dateHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFECDD3.toInt()
            textSize = 9f * scale
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("DATE", colWidth / 2f, height - 70f * scale, dateHeaderPaint)

        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 10f * scale
            textAlign = Paint.Align.CENTER
        }
        val dateLines = wrapText(data.submissionDate, datePaint, colWidth - 16f * scale)
        dateLines.forEachIndexed { i, line ->
            canvas.drawText(line, colWidth / 2f, height - 52f * scale + (i * 14f * scale), datePaint)
        }

        // 2. Right 72% White Area
        val rightLeft = colWidth + 35f * scale
        val rightWidth = width - rightLeft - 30f * scale

        // Subheader
        val tagPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = crimson
            textSize = 11f * scale
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            letterSpacing = 0.08f
        }
        canvas.drawText("ACADEMIC SUBMISSION", rightLeft, 70f * scale, tagPaint)

        // Title
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textDark
            textSize = 22f * scale
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val titleLines = wrapText(data.assignmentTitle.ifBlank { "Assignment Title" }, titlePaint, rightWidth)
        var currentY = 110f * scale
        titleLines.forEach { line ->
            canvas.drawText(line, rightLeft, currentY, titlePaint)
            currentY += 28f * scale
        }

        // Course Pill
        val course = "${data.courseCode} • ${data.courseTitle}".trim().trim('•').trim()
        if (course.isNotBlank()) {
            currentY += 10f * scale
            val coursePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = crimson
                textSize = 11f * scale
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }
            val pillWidth = coursePaint.measureText(course) + 24f * scale
            val pillRect = RectF(rightLeft, currentY - 16f * scale, rightLeft + pillWidth, currentY + 10f * scale)
            val pillBg = Paint().apply { color = 0xFFFFF1F2.toInt(); style = Paint.Style.FILL }
            canvas.drawRoundRect(pillRect, 6f * scale, 6f * scale, pillBg)
            canvas.drawText(course, rightLeft + 12f * scale, currentY, coursePaint)
            currentY += 30f * scale
        }

        // Horizontal Divider line
        currentY = 400f * scale
        val divPaint = Paint().apply { color = 0xFFE2E8F0.toInt(); strokeWidth = 1f * scale }
        canvas.drawLine(rightLeft, currentY, width - 30f * scale, currentY, divPaint)

        // Faculty Section
        currentY += 35f * scale
        val secHeader = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF64748B.toInt()
            textSize = 10f * scale
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        canvas.drawText("SUBMITTED TO", rightLeft, currentY, secHeader)

        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textDark
            textSize = 15f * scale
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        canvas.drawText(data.facultyName, rightLeft, currentY + 22f * scale, namePaint)

        val infoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF475569.toInt()
            textSize = 11.5f * scale
        }
        canvas.drawText(data.facultyDesignation, rightLeft, currentY + 40f * scale, infoPaint)
        canvas.drawText(data.facultyDepartment, rightLeft, currentY + 58f * scale, infoPaint)

        // Divider
        currentY += 95f * scale
        canvas.drawLine(rightLeft, currentY, width - 30f * scale, currentY, divPaint)

        // Student Section
        currentY += 35f * scale
        canvas.drawText("SUBMITTED BY", rightLeft, currentY, secHeader)
        canvas.drawText(data.studentName, rightLeft, currentY + 22f * scale, namePaint)

        // Highlighted Student ID badge
        val idText = "ID: ${data.studentId}"
        val idPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 10.5f * scale
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val idWidth = idPaint.measureText(idText) + 16f * scale
        val idRect = RectF(rightLeft, currentY + 34f * scale, rightLeft + idWidth, currentY + 54f * scale)
        val idBg = Paint().apply { color = crimson; style = Paint.Style.FILL }
        canvas.drawRoundRect(idRect, 4f * scale, 4f * scale, idBg)
        canvas.drawText(idText, rightLeft + 8f * scale, currentY + 48f * scale, idPaint)

        if (data.batchSection.isNotBlank()) {
            canvas.drawText(data.batchSection, rightLeft + idWidth + 12f * scale, currentY + 48f * scale, infoPaint)
        }
        if (data.department.isNotBlank()) {
            canvas.drawText(data.department, rightLeft, currentY + 70f * scale, infoPaint)
        }
    }

    // =========================================================================
    // DRAWING HELPER UTILITIES
    // =========================================================================

    private fun drawModernStackCard(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        accentColor: Int,
        header: String,
        items: List<String>,
        scale: Float
    ) {
        val rect = RectF(left, top, right, bottom)
        val bgPaint = Paint().apply { color = 0xFFF8FAFC.toInt(); style = Paint.Style.FILL }
        val borderPaint = Paint().apply { color = 0xFFE2E8F0.toInt(); style = Paint.Style.STROKE; strokeWidth = 1f * scale }
        canvas.drawRoundRect(rect, 8f * scale, 8f * scale, bgPaint)
        canvas.drawRoundRect(rect, 8f * scale, 8f * scale, borderPaint)

        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            textSize = 9.5f * scale
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        canvas.drawText(header, left + 14f * scale, top + 20f * scale, headerPaint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF0F172A.toInt()
            textSize = 12f * scale
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        items.getOrNull(0)?.let {
            canvas.drawText(it, left + 14f * scale, top + 40f * scale, textPaint)
        }

        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        textPaint.color = 0xFF475569.toInt()
        textPaint.textSize = 10.5f * scale
        items.getOrNull(1)?.let {
            canvas.drawText(it, left + 14f * scale, top + 58f * scale, textPaint)
        }
        items.getOrNull(2)?.let {
            canvas.drawText(it, left + 14f * scale, top + 74f * scale, textPaint)
        }
    }

    private fun drawFloatingCard(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        accentColor: Int,
        header: String,
        items: List<String>,
        scale: Float
    ) {
        val rect = RectF(left, top, right, bottom)
        val bg = Paint().apply { color = 0xFFF8FAFC.toInt(); style = Paint.Style.FILL }
        val border = Paint().apply { color = 0xFFE2E8F0.toInt(); style = Paint.Style.STROKE; strokeWidth = 1.2f * scale }
        canvas.drawRoundRect(rect, 10f * scale, 10f * scale, bg)
        canvas.drawRoundRect(rect, 10f * scale, 10f * scale, border)

        // Pill header
        val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 9f * scale
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val pillW = pillPaint.measureText(header) + 16f * scale
        val pillRect = RectF(left + 12f * scale, top + 10f * scale, left + 12f * scale + pillW, top + 28f * scale)
        val pillBg = Paint().apply { color = accentColor; style = Paint.Style.FILL }
        canvas.drawRoundRect(pillRect, 4f * scale, 4f * scale, pillBg)
        canvas.drawText(header, left + 20f * scale, top + 23f * scale, pillPaint)

        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF0F172A.toInt()
            textSize = 12f * scale
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        items.getOrNull(0)?.let { canvas.drawText(it, left + 12f * scale, top + 52f * scale, namePaint) }

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF475569.toInt()
            textSize = 10.5f * scale
        }
        items.getOrNull(1)?.let { canvas.drawText(it, left + 12f * scale, top + 74f * scale, subPaint) }
        items.getOrNull(2)?.let { canvas.drawText(it, left + 12f * scale, top + 92f * scale, subPaint) }
    }

    private fun drawTechDataBox(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        accentColor: Int,
        header: String,
        rows: List<String>,
        scale: Float
    ) {
        val rect = RectF(left, top, right, bottom)
        val border = Paint().apply { color = accentColor; style = Paint.Style.STROKE; strokeWidth = 1.2f * scale }
        canvas.drawRect(rect, border)

        val headerBg = Paint().apply { color = accentColor; style = Paint.Style.FILL }
        canvas.drawRect(left, top, right, top + 24f * scale, headerBg)

        val hText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 10f * scale
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }
        canvas.drawText(header, left + 8f * scale, top + 17f * scale, hText)

        val rowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF0F172A.toInt()
            textSize = 10.5f * scale
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        }
        rows.forEachIndexed { i, row ->
            canvas.drawText(row, left + 8f * scale, top + 50f * scale + (i * 22f * scale), rowPaint)
        }
    }

    private fun drawProfileCard(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        accentColor: Int,
        header: String,
        title: String,
        subtitle: String,
        scale: Float
    ) {
        val rect = RectF(left, top, right, bottom)
        val bg = Paint().apply { color = 0xFFF8FAFC.toInt(); style = Paint.Style.FILL }
        val border = Paint().apply { color = 0xFFE2E8F0.toInt(); style = Paint.Style.STROKE; strokeWidth = 1f * scale }
        canvas.drawRoundRect(rect, 10f * scale, 10f * scale, bg)
        canvas.drawRoundRect(rect, 10f * scale, 10f * scale, border)

        // Left Icon avatar circle
        val circleRect = RectF(left + 12f * scale, top + (bottom - top - 36f * scale) / 2f, left + 48f * scale, top + (bottom - top + 36f * scale) / 2f)
        val circlePaint = Paint().apply { color = accentColor; style = Paint.Style.FILL }
        canvas.drawOval(circleRect, circlePaint)

        val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 14f * scale
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("★", circleRect.centerX(), circleRect.centerY() + 5f * scale, starPaint)

        // Text
        val textX = left + 60f * scale
        val hPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            textSize = 9.5f * scale
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        canvas.drawText(header, textX, top + 25f * scale, hPaint)

        val tPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF0F172A.toInt()
            textSize = 13f * scale
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        canvas.drawText(title, textX, top + 47f * scale, tPaint)

        val sPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF64748B.toInt()
            textSize = 10.5f * scale
        }
        canvas.drawText(subtitle, textX, top + 67f * scale, sPaint)
    }

    private fun drawLogoOrFallback(
        canvas: Canvas,
        logoBitmap: Bitmap?,
        rect: RectF,
        accentColor: Int,
        scale: Float
    ) {
        if (logoBitmap != null) {
            canvas.drawBitmap(logoBitmap, null, rect, null)
        } else {
            val emblemBorder = Paint().apply {
                color = accentColor
                style = Paint.Style.STROKE
                strokeWidth = 2f * scale
            }
            canvas.drawOval(rect, emblemBorder)

            val innerCircle = Paint().apply {
                color = accentColor
                style = Paint.Style.STROKE
                strokeWidth = 1f * scale
            }
            val insetRect = RectF(rect.left + 5f * scale, rect.top + 5f * scale, rect.right - 5f * scale, rect.bottom - 5f * scale)
            canvas.drawOval(insetRect, innerCircle)

            val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = accentColor
                textSize = 18f * scale
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("★", rect.centerX(), rect.centerY() + 6f * scale, starPaint)
        }
    }

    private fun decodeUserLogo(context: Context, logoUriStr: String?): Bitmap? {
        if (logoUriStr.isNullOrBlank()) return null
        return try {
            val uri = Uri.parse(logoUriStr)
            val inputStream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (_: Exception) {
            null
        }
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val width = paint.measureText(testLine)
            if (width <= maxWidth) {
                currentLine = testLine
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine)
                }
                currentLine = word
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }
        return if (lines.isEmpty()) listOf(text) else lines
    }
}
