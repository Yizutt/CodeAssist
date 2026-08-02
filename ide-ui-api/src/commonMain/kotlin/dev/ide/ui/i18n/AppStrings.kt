package dev.ide.ui.i18n

/**
 * Runtime-localized strings for the app shell (settings, navigation, build console, AI agent).
 * Each entry maps a key → locale code → translated text. Look up with [lookup].
 */
object AppStrings {

    private val data: Map<String, Map<String, String>> = buildMap {
        // --- settings pages ---------------------------------------------------------------
        put("settings_appearance", l("Appearance", "外观", "المظهر", "Apariencia", "Tampilan", "Aparência", "Внешний вид"))
        put("settings_editor", l("Editor", "编辑器", "المحرر", "Editor", "Penyunting", "Editor", "Редактор"))
        put("settings_completion", l("Code Completion", "代码补全", "إكمال الكود", "Completado de código", "Pelengkapan Kode", "Conclusão de código", "Автодополнение"))
        put("settings_analysis", l("Analysis & Inspections", "分析与检查", "التحليل والفحوصات", "Análisis e inspecciones", "Analisis & Inspección", "Análise & Inspeções", "Анализ и инспекции"))
        put("settings_preview", l("Preview", "预览", "معاينة", "Vista previa", "Pratinjau", "Visualização", "Предпросмотр"))
        put("settings_build", l("Build", "构建", "بناء", "Compilación", "Build", "Compilação", "Сборка"))
        put("settings_buildRuntime", l("Build Runtime", "构建运行时", "وقت تشغيل البناء", "Tiempo de ejecución de compilación", "Waktu Build", "Tempo de Execução de Compilação", "Время сборки"))
        put("settings_privacy", l("Privacy", "隐私", "الخصوصية", "Privacidad", "Privasi", "Privacidade", "Конфиденциальность"))
        put("settings_ai", l("AI Agent", "AI 助手", "وكيل الذكاء الاصطناعي", "Agente IA", "Agen AI", "Agente de IA", "AI-агент"))

        // --- appearance controls ---------------------------------------------------------
        put("theme", l("Theme", "主题", "السمة", "Tema", "Tema", "Tema", "Тема"))
        put("theme_light", l("Light", "浅色", "فاتح", "Claro", "Terang", "Claro", "Светлая"))
        put("theme_dark", l("Dark", "深色", "داكن", "Oscuro", "Gelap", "Escuro", "Тёмная"))
        put("theme_system", l("System", "跟随系统", "النظام", "Sistema", "Sistema", "Sistema", "Системная"))
        put("accent", l("Accent", "强调色", "اللون المميز", "Acento", "Aksen", "Destaque", "Акцент"))
        put("language", l("Language", "语言", "اللغة", "Idioma", "Bahasa", "Idioma", "Язык"))

        // --- build console ---------------------------------------------------------------
        put("build_idle", l("Idle", "空闲", "خامل", "Inactivo", "Idle", "Ocioso", "Ожидание"))
        put("build_running", l("Building…", "构建中…", "جاري البناء…", "Compilando…", "Membangun…", "Compilando…", "Сборка…"))
        put("build_success", l("Build succeeded", "构建成功", "نجح البناء", "Compilación exitosa", "Build berhasil", "Compilação bem-sucedida", "Сборка успешна"))
        put("build_failed", l("Build failed", "构建失败", "فشل البناء", "Compilación fallida", "Build gagal", "Compilação falhou", "Ошибка сборки"))

        // --- project view ----------------------------------------------------------------
        put("projects", l("Projects", "项目", "المشاريع", "Proyectos", "Proyek", "Projetos", "Проекты"))
        put("files", l("Files", "文件", "الملفات", "Archivos", "Berkas", "Arquivos", "Файлы"))
        put("no_project", l("No project is open", "未打开项目", "لا يوجد مشروع مفتوح", "No hay proyecto abierto", "Tidak ada proyek", "Nenhum projeto aberto", "Проект не открыт"))

        // --- AI agent --------------------------------------------------------------------
        put("ai_chat_title", l("AI", "AI助手", "AI مساعد", "IA", "AI", "IA", "AI"))
        put("ai_new", l("New", "新会话", "جديد", "Nuevo", "Baru", "Novo", "Новый"))
        put("ai_close", l("Close", "关闭", "إغلاق", "Cerrar", "Tutup", "Fechar", "Закрыть"))
        put("ai_placeholder", l("Ask anything…", "输入问题…", "اسأل أي شيء…", "Pregunta algo…", "Tanya apa saja…", "Pergunte qualquer coisa…", "Спросите что угодно…"))
        put("ai_send", l("Send", "发送", "إرسال", "Enviar", "Kirim", "Enviar", "Отправить"))
        put("ai_stop", l("Stop", "停止", "إيقاف", "Detener", "Berhenti", "Parar", "Стоп"))
        put("ai_thinking", l("Thinking…", "思考中…", "جاري التفكير…", "Pensando…", "Berpikir…", "Pensando…", "Думаю…"))
        put("ai_need_key", l("Add an API key to use the agent.", "请添加 API 密钥。", "أضف مفتاح API.", "Agrega una clave API.", "Tambahkan kunci API.", "Adicione uma chave API.", "Добавьте ключ API."))
        put("ai_add_key", l("Add key", "添加密钥", "أضف مفتاح", "Agregar clave", "Tambahkan kunci", "Adicionar chave", "Добавить ключ"))
        put("ai_manage_keys", l("Manage keys", "管理密钥", "إدارة المفاتيح", "Gestionar claves", "Kelola kunci", "Gerenciar chaves", "Управление ключами"))
        put("ai_retry", l("Retry", "重试", "إعادة المحاولة", "Reintentar", "Coba lagi", "Tentar novamente", "Повторить"))
        put("ai_copy", l("Copy", "复制", "نسخ", "Copiar", "Salin", "Copiar", "Копировать"))
        put("ai_copied", l("Copied!", "已复制!", "تم النسخ!", "¡Copiado!", "Disalin!", "Copiado!", "Скопировано!"))
        put("ai_mode_ask", l("Ask", "询问", "اسأل", "Preguntar", "Tanya", "Perguntar", "Спросить"))
        put("ai_mode_auto", l("Auto", "自动", "تلقائي", "Auto", "Otomatis", "Automático", "Авто"))
        put("ai_mode_plan", l("Plan", "只规划", "تخطيط فقط", "Solo plan", "Rencana", "Só planejar", "Только план"))
        put("ai_tools_off", l("Tools OFF", "工具关闭", "إيقاف الأدوات", "Herramient. OFF", "Alat MATI", "Ferramentas DESL.", "Инструменты ВЫКЛ"))
        put("ai_tools_unlimited", l("∞ tools", "∞ 工具", "∞ أدوات", "∞ herramient.", "∞ alat", "∞ ferramentas", "∞ инструментов"))
        put("ai_history", l("History", "历史", "السجل", "Historial", "Riwayat", "Histórico", "История"))
        put("ai_no_history", l("No history yet.", "暂无历史。", "لا يوجد سجل بعد.", "Sin historial aún.", "Belum ada riwayat.", "Sem histórico ainda.", "Истории пока нет."))
        put("ai_delete", l("Delete", "删除", "حذف", "Eliminar", "Hapus", "Excluir", "Удалить"))
        put("ai_export", l("Export", "导出", "تصدير", "Exportar", "Ekspor", "Exportar", "Экспорт"))
        put("ai_import", l("Import", "导入", "استيراد", "Importar", "Impor", "Importar", "Импорт"))
        put("ai_tokens", l("%1 tokens · %2 turns", "%1 Token · %2 轮", "%1 رمز · %2 جولة", "%1 tokens · %2 turnos", "%1 token · %2 giliran", "%1 tokens · %2 turnos", "%1 токен · %2 ходов"))
    }

    /** Look up [key] for [locale], falling back to English then the key itself. */
    fun lookup(key: String, locale: String?): String {
        val map = data[key] ?: return key
        if (locale != null) map[locale]?.let { return it }
        map["en"]?.let { return it }
        return key
    }

    /** Convenience: build a locale map from positional args (en, zh, ar, es, `in`, ptBR, ru). */
    private fun l(en: String, zh: String, ar: String, es: String, indo: String, ptBR: String, ru: String): Map<String, String> = mapOf(
        "en" to en,
        "zh" to zh,
        "ar" to ar,
        "es" to es,
        "in" to indo,
        "pt-BR" to ptBR,
        "ru" to ru,
    )
}
