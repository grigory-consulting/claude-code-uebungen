export const meta = {
  name: 'geldregeln-audit',
  description: 'Je übergebene Datei die Geldregeln prüfen, jeden Befund gegenprüfen (Haiku, nur lesend)',
  phases: [{ title: 'Prüfen' }, { title: 'Gegenprüfen' }],
}
const FILES = args
const RULES = `Regeln aus docs/team-standards.md:
1. Geldbeträge nur als de.exercise.core.Money. Kein double, float, rohes BigDecimal für Beträge (auch nicht als Zwischenwert). Faktoren/Prozentsätze dürfen BigDecimal sein.
2. Runden nur über de.exercise.core.Rounding (via Money-Methoden), nie RoundingMode oder Math.round.
3. Keine festen Steuersätze, Fristen oder Gebühren als Konstante/Literal im Fachcode; sie kommen aus Configuration.`
const FINDINGS = {
  type: 'object',
  properties: { findings: { type: 'array', items: { type: 'object', properties: {
    line: { type: 'number' }, rule: { type: 'string' }, code: { type: 'string' }, reason: { type: 'string' } },
    required: ['line', 'rule', 'code', 'reason'] } } },
  required: ['findings'],
}
const VERDICT = {
  type: 'object',
  properties: { confirmed: { type: 'boolean' }, reason: { type: 'string' } },
  required: ['confirmed', 'reason'],
}
const results = await pipeline(
  FILES,
  f => agent(`Lies ${f} vollständig (nur lesen, nichts ändern) und prüfe sie auf Verstöße gegen diese Regeln:\n${RULES}\nMelde jeden Verstoß mit exakter Zeilennummer. Keine Verstöße: leere Liste. Spekuliere nicht.`,
    { model: 'haiku', phase: 'Prüfen', label: 'prüfen:' + f.split('/').pop(), schema: FINDINGS }),
  (r, f) => parallel((r?.findings ?? []).map(fd => () =>
    agent(`Gegenprüfung. Lies ${f} (nur lesen) und prüfe kritisch, ob dieser Befund echt ist. Im Zweifel confirmed=false.\nZeile ${fd.line}: ${fd.code}\nRegel: ${fd.rule}\nBegründung: ${fd.reason}\n${RULES}\nBeachte: Faktoren/Prozentsätze als BigDecimal sind erlaubt; Konstanten, die nicht Steuersatz/Frist/Gebühr sind, sind kein Verstoß. Prüfe, ob die Zeilennummer stimmt.`,
      { model: 'haiku', phase: 'Gegenprüfen', label: `prüfen:${f.split('/').pop()}:${fd.line}`, schema: VERDICT })
      .then(v => ({ file: f, ...fd, verdict: v }))))
)
const all = results.filter(Boolean).flat().filter(Boolean)
log(`${all.length} Befunde, ${all.filter(x => x.verdict?.confirmed).length} bestätigt`)
return { confirmed: all.filter(x => x.verdict?.confirmed), rejected: all.filter(x => !x.verdict?.confirmed) }