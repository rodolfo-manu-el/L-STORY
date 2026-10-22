package com.example.data.export

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.StoryChapter
import com.example.data.model.StoryCharacter
import com.example.data.model.StoryProject
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

enum class ExportFormat(val extension: String, val mimeType: String, val label: String) {
    PDF("pdf", "application/pdf", "PDF Editorial (Maquetado para lectura e impresión)"),
    EPUB("epub", "application/epub+zip", "EPUB Digital (Formato estándar de e-Readers)"),
    DOCX("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "DOCX Manuscrito (Compatible con Word / Docs)")
}

data class ExportResult(
    val format: ExportFormat,
    val file: File,
    val summaryMessage: String,
    val previewSnippet: String
)

object ManuscriptExporter {

    fun exportProject(
        context: Context,
        project: StoryProject,
        chapters: List<StoryChapter>,
        characters: List<StoryCharacter>,
        format: ExportFormat
    ): Result<ExportResult> = runCatching {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val safeTitle = project.title.replace(Regex("[^a-zA-Z0-9áéíóúÁÉÍÓÚñÑ_\\-]"), "_").ifBlank { "Manuscrito" }
        val outFile = File(exportDir, "${safeTitle}_StoryBuilder.${format.extension}")

        when (format) {
            ExportFormat.PDF -> generatePdf(outFile, project, chapters, characters)
            ExportFormat.EPUB -> generateEpub(outFile, project, chapters, characters)
            ExportFormat.DOCX -> generateDocxPackage(outFile, project, chapters, characters)
        }

        val totalWords = chapters.sumOf { it.wordCount }
        val preview = buildString {
            appendLine("TÍTULO: ${project.title.uppercase()}")
            appendLine("GÉNERO: ${project.genre} · ESTRUCTURA: ${project.structureTemplate}")
            appendLine("CAPÍTULOS: ${chapters.size} · PALABRAS TOTALES: $totalWords")
            appendLine("ARCHIVO GENERADO: ${outFile.name} (${outFile.length() / 1024} KB)")
            appendLine("────────────────────────────────────────")
            chapters.take(2).forEach { ch ->
                appendLine("\n${ch.actLabel} — ${ch.title}")
                appendLine(ch.content.ifBlank { ch.synopsis }.take(280))
            }
        }

        ExportResult(
            format = format,
            file = outFile,
            summaryMessage = "Manuscrito exportado en ${format.name} (${outFile.name})",
            previewSnippet = preview
        )
    }

    fun shareExportedFile(context: Context, exportResult: ExportResult) {
        val authority = "${context.packageName}.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, exportResult.file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = exportResult.format.mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Manuscrito StoryBuilder: ${exportResult.file.nameWithoutExtension}")
            putExtra(Intent.EXTRA_TEXT, exportResult.previewSnippet)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Compartir o guardar manuscrito").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    private fun generatePdf(
        outFile: File,
        project: StoryProject,
        chapters: List<StoryChapter>,
        characters: List<StoryCharacter>
    ) {
        val pdfDocument = PdfDocument()
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(25, 22, 43)
            textSize = 24f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(120, 95, 50)
            textSize = 13f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC)
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(35, 32, 45)
            textSize = 11.5f
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        }

        // Page 1: Cover & Bible Overview
        val coverInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val coverPage = pdfDocument.startPage(coverInfo)
        val canvas: Canvas = coverPage.canvas
        canvas.drawColor(Color.rgb(250, 244, 232))
        canvas.drawText(project.title.take(40), 60f, 140f, titlePaint)
        canvas.drawText("Género: ${project.genre} | Plantilla: ${project.structureTemplate}", 60f, 175f, subtitlePaint)
        drawMultilineText(canvas, "Premisa: ${project.premise}", 60f, 220f, 475f, bodyPaint)
        drawMultilineText(canvas, "Protagonista: ${project.protagonistName} — Secreto: ${project.protagonistSecret}", 60f, 300f, 475f, bodyPaint)
        drawMultilineText(canvas, "Conflicto Central: ${project.centralConflict}", 60f, 360f, 475f, bodyPaint)

        var yPos = 430f
        canvas.drawText("Dramatis Personae (Fichas de Personajes)", 60f, yPos, subtitlePaint)
        yPos += 25f
        characters.take(5).forEach { char ->
            yPos = drawMultilineText(
                canvas,
                "• ${char.name} (${char.role}): Motivación: ${char.motivation} | Arco: ${char.evolutionArc}",
                60f,
                yPos,
                475f,
                bodyPaint
            ) + 10f
        }
        pdfDocument.finishPage(coverPage)

        // Subsequent Pages: Chapters
        val effectiveChapters = if (chapters.isEmpty()) {
            listOf(
                StoryChapter(
                    chapterNumber = 1,
                    actLabel = "Acto I",
                    title = "Capítulo 1",
                    synopsis = project.premise,
                    content = project.premise
                )
            )
        } else {
            chapters
        }

        effectiveChapters.forEachIndexed { index, chapter ->
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, index + 2).create()
            val page = pdfDocument.startPage(pageInfo)
            val chCanvas = page.canvas
            chCanvas.drawColor(Color.rgb(250, 244, 232))
            chCanvas.drawText(chapter.actLabel, 60f, 75f, subtitlePaint)
            chCanvas.drawText("Capítulo ${chapter.chapterNumber}: ${chapter.title}", 60f, 105f, titlePaint)
            val textToDraw = chapter.content.ifBlank { "Escaleta: ${chapter.synopsis}" }
            drawMultilineText(chCanvas, textToDraw, 60f, 150f, 475f, bodyPaint)
            pdfDocument.finishPage(page)
        }

        FileOutputStream(outFile).use { pdfDocument.writeTo(it) }
        pdfDocument.close()
    }

    private fun drawMultilineText(
        canvas: Canvas,
        text: String,
        x: Float,
        startY: Float,
        maxWidth: Float,
        paint: Paint
    ): Float {
        var y = startY
        val words = text.replace("\n", " \n ").split(" ")
        var line = StringBuilder()
        for (word in words) {
            if (word == "\n") {
                canvas.drawText(line.toString(), x, y, paint)
                line = StringBuilder()
                y += 18f
                if (y > 780f) break
                continue
            }
            val testLine = if (line.isEmpty()) word else "$line $word"
            if (paint.measureText(testLine) > maxWidth) {
                canvas.drawText(line.toString(), x, y, paint)
                line = StringBuilder(word)
                y += 18f
                if (y > 780f) break
            } else {
                line = StringBuilder(testLine)
            }
        }
        if (line.isNotEmpty() && y <= 780f) {
            canvas.drawText(line.toString(), x, y, paint)
            y += 18f
        }
        return y
    }

    private fun generateEpub(
        outFile: File,
        project: StoryProject,
        chapters: List<StoryChapter>,
        characters: List<StoryCharacter>
    ) {
        ZipOutputStream(FileOutputStream(outFile)).use { zip ->
            zip.putNextEntry(ZipEntry("mimetype"))
            zip.write("application/epub+zip".toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            zip.putNextEntry(ZipEntry("OEBPS/manuscript.xhtml"))
            val xhtml = buildString {
                appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                appendLine("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><title>${project.title}</title></head><body>")
                appendLine("<h1>${project.title}</h1>")
                appendLine("<p><em>${project.genre} — ${project.premise}</em></p>")
                appendLine("<h2>Personajes</h2><ul>")
                characters.forEach { c ->
                    appendLine("<li><strong>${c.name} (${c.role}):</strong> ${c.motivation}</li>")
                }
                appendLine("</ul><hr/>")
                chapters.forEach { ch ->
                    appendLine("<h2>Capítulo ${ch.chapterNumber}: ${ch.title}</h2>")
                    appendLine("<p><em>${ch.actLabel} — ${ch.synopsis}</em></p>")
                    ch.content.split("\n\n").forEach { para ->
                        if (para.isNotBlank()) appendLine("<p>${para.trim()}</p>")
                    }
                }
                appendLine("</body></html>")
            }
            zip.write(xhtml.toByteArray(Charsets.UTF_8))
            zip.closeEntry()
        }
    }

    private fun generateDocxPackage(
        outFile: File,
        project: StoryProject,
        chapters: List<StoryChapter>,
        characters: List<StoryCharacter>
    ) {
        ZipOutputStream(FileOutputStream(outFile)).use { zip ->
            zip.putNextEntry(ZipEntry("word/document.xml"))
            val xml = buildString {
                appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
                appendLine("<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:body>")
                appendLine("<w:p><w:r><w:t>${project.title} (${project.genre})</w:t></w:r></w:p>")
                appendLine("<w:p><w:r><w:t>Premisa: ${project.premise}</w:t></w:r></w:p>")
                characters.forEach { c ->
                    appendLine("<w:p><w:r><w:t>Personaje: ${c.name} [${c.role}] - ${c.motivation}</w:t></w:r></w:p>")
                }
                chapters.forEach { ch ->
                    appendLine("<w:p><w:r><w:t>Capítulo ${ch.chapterNumber}: ${ch.title}</w:t></w:r></w:p>")
                    appendLine("<w:p><w:r><w:t>${ch.content.ifBlank { ch.synopsis }}</w:t></w:r></w:p>")
                }
                appendLine("</w:body></w:document>")
            }
            zip.write(xml.toByteArray(Charsets.UTF_8))
            zip.closeEntry()
        }
    }
}
