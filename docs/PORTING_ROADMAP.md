# Карта доведения порта SRC → LEGACY

Обновлено: 2026-10-02. Основание: независимый статический аудит от 2026-09-15 и повторная сверка от 2026-10-02, Git `b918a0b`.

## Состояние и цель

**LEGACY — частичный порт с подтверждёнными дефектами. Функциональная эквивалентность SRC не достигнута.** Успешная сборка подтверждает совместимость компиляции, но не полноту переноса.

Цель: восстановить наблюдаемое поведение `src/main` в Forge 1.7.10 (`src/legacy`): игровые условия, действия, расход ресурсов, сохранение, синхронизацию, интерфейсы и расширения. Простого устранения 34 дефектов недостаточно: остаются упрощённые и отсутствующие механики.

Этот документ заменяет прежние карту, release audit, regression matrix и отдельный отчёт аудита. Подробные доказательства и сценарии перенесены ниже. Markdown в ресурсах мода не относится к документации портирования.

## Повторная общая сверка — 2026-10-02

**Вывод не изменился: порт функционально неполон, известные дефекты не исправлены.** Сравнение SHA-256 всех Java-файлов с сохранённым снимком установило: SRC — 1488, LEGACY — 198; изменённых, добавленных и удалённых Java-файлов нет. Это подтверждает актуальность прежней базы аудита, но не означает новую построчную проверку всех файлов.

Повторно прочитаны ключевые пути алтаря/gohei, кровати, фермерства, рюкзаков, контейнера/рендера рук, миграции NBT, Film/UUID, honey, очередей и сборки. Сопоставлены исходные пути создания алтаря, посадки, конфигурации и удаления кровати. Дополнительно найден дефект №29; всего документировано 29 дефектов (14 P1, 15 P2).

- `gradlew.bat --offline build`: успешно, reobf выполнена; `test NO-SOURCE`.
- `gradlew.bat --offline compileJava --rerun-tasks`: первая попытка скомпилировала код, но завершилась ошибкой освобождения fileHashes cache. Повторный запуск завершился успешно, все три задачи выполнены. Кэш вручную не удалялся. Предупреждения deprecated/unchecked остаются.
- Повторная инспекция ZIP: production `0.1.0-port.jar` содержит 13 классов движков, `0.1.0-port-dev.jar` — 0. Ошибка упаковки №24 сохраняется.
- Новые игровые испытания не запускались. Dedicated, два клиента, визуальная правильность, поведение под нагрузкой и фактическая миграция сохранений не подтверждены этой сверкой.
- Изменена только карта; игровой код не исправлялся. Приоритет — сохранность данных и базовый survival-цикл, затем полный перенос поведения подсистем.

## Исправления предметных ресурсов и локализации — 2026-10-02

После повторной сверки внесены изменения в игровой код и ресурсы. Утверждение выше о неизменности Java относится к состоянию **до этого исправления**; старый SHA-256 снимок сохранён как историческая база.

- Smart Slab INIT использует исходную иконку `smart_slab_has_maid`; ссылки на отсутствующий `smart_slab_init.png` больше нет.
- Подключён клиентский `LegacyItemRenderer`: 11 предметов мебели получают исходные иконки вместо приближённых блоков в инвентаре/руках/дропе. Для picnic_mat используется иконка picnic_basket как соответствие предмету размещения.
- Для камеры, огнетушителя, двух gohei, маяка и шкафа с едой подключены шесть исходных JSON-моделей вне инвентаря: геометрия, UV, вращения граней/элементов и display transforms. В инвентаре используются исходные плоские иконки. Иконки подключены через item atlas, включая анимации keyboard/scarecrow/camera из `.mcmeta`. При перезагрузке ресурсов модели перечитываются; при ошибке загрузки остаётся иконка и запись в журнале.
- Арбалет, трезубец и бутылочка мёда получили соответствующие vanilla-иконки вместо лука, железного меча и бутылки зелья. Использованы неизменённые ресурсы локального Minecraft 1.21.10; происхождение и SHA-256 записаны в `ITEM_TEXTURE_SOURCES.json`. Это изменение внешнего вида, а не перенос механик оружия.
- В 13 `.lang` добавлены доступные исходные переводы под ключами 1.7.10: `item.*.name`, `tile.*.name`, названия профессий и расписаний. В ru_RU заполнены недостающие названия предметов, исправлены опечатки и терминология. Сообщения предметов переведены на ключи с русскими и английскими значениями, параметры сохранены. Для остальных языков новые сообщения используют стандартный английский fallback.
- GUI профессий/расписания теперь использует переводы, обрезая надпись по ширине в пикселях, а не по числу символов.

Проверка: `gradlew.bat --offline build` (компиляция и reobf), `python docs/verify_item_resources.py` (ресурсы production JAR, 53 предмета, 11 sprite renderers, 6 JSON-моделей / 336 граней, 13 языков, обязательные ключи en_US/ru_RU и параметры сообщений). Скрипт не проверяет пиксельную правильность изображения на экране.

Остаётся визуальная приёмка: все новые модели в руках игрока/горничной, дроп, масштаб/UV/освещение и resource reload. Динамические предметные модели стула и фигурки с NBT не восстановлены этим изменением; их нельзя считать эквивалентными SRC. Полнота анимационного движка моделей горничных также не затронута. Этап 7 остаётся частичным.

## Проверка EntityMaid и MaidUI — 2026-10-02

Под MaidUI здесь понимаются `AbstractGuiMaid`, четыре экрана `GuiMaid*`, их контейнеры и пакеты; отдельного класса MaidUI в LEGACY нет. Проверены серверный tick и взаимодействие, NBT, DataWatcher, обе руки, открытие/переключение вкладок, ограничения слотов, расписание и отображение показателей. Использован также предоставленный пользователем [снимок текущего UI](C:/Users/brawl/Desktop/mods/TLMM/docs/images/maid-ui-2026-10-02.png).

**EntityMaid и UI нельзя принять как корректный перенос.** Повторно подтверждены №06–09, 14–15, 18: переносной верстак, вместимость рюкзака на клиенте, профессия после загрузки, руки при tracking, питание/сон и WirelessIO. Найдены ещё пять проблем №30–34. Снимок подтверждает визуальные дефекты №34; он не доказывает сетевые ошибки или прохождение игровых сценариев.

Положительные результаты: контейнеры проверяют живую сущность, владельца и дистанцию <8 блоков; действия UI исполняются через серверную очередь; direct-select проверяет ID профессии и длину строки; фильтры типов брони и соответствие шести слотов экипировки согласованы. Это не полный adversarial-тест сети. Вложения/состояние слотов при изменении условий и экран настройки профессии остаются неполным переносом.

Эта проверка не меняла игровой код и не запускала клиент/сервер. Новая сборка ради чтения кода не выполнялась. Приоритет исправления: единое синхронизированное состояние EntityMaid → согласованные ограничения контейнеров → обновление кнопок → исходная компоновка статусов и подсказки. Визуальная перекраска сама по себе не исправит ошибки поведения.

## Исправления EntityMaid / MaidUI и контроль предыдущих изменений — 2026-10-02

Этот раздел описывает изменения **после** приведённого выше аудита. Нумерованные находки ниже сохранены как историческое описание причин; они не означают, что каждое исходное проявление всё ещё воспроизводится.

- №06: переносной верстак получил отдельный ContainerMaidCrafting с проверкой владельца, дистанции и установленного рюкзака; рецепты и остатки обрабатывает ContainerWorkbench 1.7.10, привязка к блоку верстака устранена.
- №07–09: вместимость читает синхронизированный тип рюкзака; загрузка NBT обновляет watcher профессии; основная рука использует vanilla equipment sync, вторая — ItemStack DataWatcher. Рендер читает синхронизированные руки.
- №13: добавлены aliases MaidIsHome/MaidIsPickup при миграции, без перезаписи существующих LEGACY-значений. Остальные схемы миграции требуют отдельной проверки.
- №14, частично: убран искусственный периодический урон от голода, добавлена самостоятельная еда из рук/доступного рюкзака с WorkMeal cooldown и callback ItemFood через изолированного FakePlayer. Восстановлена естественная регенерация SRC (1 HP с вероятностью 0.0025 за tick). Опасная vanilla-еда исключена по стандартному списку SRC с учётом fish:3 в 1.7.10. Настраиваемые списки, полная анимация еды и домашний цикл питания ещё не эквивалентны SRC.
- №15, частично: сон требует режима дома и REST, не начинается при сидении; проверяются измерение и занятость кровати, убран маршрут к нулевой точке при отсутствии кровати. Полный цикл домашнего поведения ещё требует сверки.
- №18, частично: WirelessIO работает из доступных bauble-слотов, с проверкой измерения, загруженного блока и радиуса текущей активности. Исходная настройка переноса по слотам не восстановлена.
- №30, 33: единый лимит аксессуаров 10/20/30 для прямого надевания, контейнера и эффектов. В контейнере всегда 30 слотов, поэтому ID слотов игрока не меняются с уровнем. Старые предметы из заблокированных ячеек можно забрать, их эффекты отключены.
- №31–32: шкала привязанности показывает прогресс внутри текущего уровня; расписание, состояния H/P/S и доступность рюкзака обновляются по синхронизированному состоянию.
- №34: убраны перекрывающиеся подписи статусов; добавлены исходные иконки, компактные значения и подсказки. Название горничной перенесено в подсказку портрета; inventory, экипировка, профессия и элементы управления локализованы. Длинные названия профессий доступны в подсказках.

Проверки: полная `gradlew.bat --offline build --rerun-tasks` прошла, включая reobf. Проверка production JAR подтвердила 53 предмета, 11 sprite renderers, 6 моделей / 336 граней, 13 языков без ошибок. `git diff --check -- src docs` — без ошибок. Добавлена игровая команда `/tlmmaid uiverify` для порогов привязанности, стабильности слотов, ограничений аксессуаров, watcher профессии, вместимости и переносного верстака; **сама команда пока не запускалась**.

Проверка подозрения на перезапись IDE: SHA-256 222 исходных/ресурсных файлов и build.gradle до и после полной сборки совпали. Это исключает перезапись проверенных файлов в этом интервале, но не устанавливает причину возможных изменений ранее. Файлы .idea, .gradle, журналы и сохранения пользователя не откатывались. Предыдущие исправления предметных ресурсов и локализации присутствуют и проходят проверку JAR.

Игровая визуальная приёмка, dedicated server и два клиента пока не выполнены. Скриншот в карте относится к версии до этих исправлений. Ни EntityMaid, ни UI, ни порт целиком ещё не отмечены как принятые.

## Правила статусов

- **Частично** — исполняемый код есть, полнота поведения не подтверждена или есть известные пропуски.
- **Дефект** — статически подтверждено ошибочное поведение; номер ведёт к подробному разбору ниже.
- **Замена** — исходная механика заменена другой; требуется полноценная реализация либо явно описанная адаптация к 1.7.10.
- **Не проверено** — недостаточно доказательств; наличие класса или регистрации не меняет статус.
- **Принято** — сопоставлены условия и результаты SRC/LEGACY, выполнены соответствующие игровые сценарии, записаны версия и результаты. Сейчас ни одна подсистема целиком этот статус не получила.

Для каждого закрываемого пункта записывать: исходный класс/метод → реализация LEGACY → отличия 1.7.10 → сценарий → фактический результат → commit/сборка и среда. Непереносимые механики перечислять отдельно с обоснованием; заглушки не считать завершённым переносом. Проценты по количеству файлов не использовать.

## Порядок работ и условия завершения

Все пункты ниже открыты. Приоритет обозначает порядок, а не утверждение о готовности остальных функций. Параллельно с каждым этапом проверять его GUI, пакеты и NBT.

| Этап | Что восстановить | Основание / зависимости | Условие завершения |
|---|---|---|---|
| 0. Основа проверки | Зафиксировать контракты SRC для каждой функции, состав production/dev JAR, запуск клиента и dedicated server, расширяемый self-test | Дефекты 24–25; основа всех этапов | Оба JAR содержат движки; оба режима запускаются; дополнительная профессия не ломает запуск; сохранены журналы |
| 1. Целостность данных | Размещение кровати, списание алтаря, импорт предметов/экипировки, Film и восстановление, защита от повторного UUID | Дефекты 03–04, 12–13, 28–29; до тестов на ценных сохранениях | Нет дюпа/тихой потери; полный NBT сохраняется; неподдерживаемые данные явно сохраняются/отклоняются; restore проверен при выгруженном оригинале |
| 2. Базовая горничная | Единственный источник состояния инвентарей, синхронизация рук/профессии/рюкзака; питание, отдых, home/follow, ownership | Дефекты 07–09, 14–15; после 1 | Два клиента видят одинаковое состояние до открытия GUI и после reload; еда, сон, следование работают по условиям SRC |
| 3. Выживание и алтарь | Gohei и создание структуры, отдельный контейнер, рецепты/остатки/Power, достижимость предметов | Дефекты 01–04, 20; после 1–2 | Новый survival-мир проходит цепочку получения горничной и рюкзаков без команд; корректны неудачный крафт и полные стаки |
| 4. Рюкзаки и аксессуары | Печь с отдельным состоянием, переносной верстак, бак с входом/выходом, WirelessIO, эффекты и прочность baubles, события favorability | Дефекты 05–07, 18–19, 27; после 1–2 | Баланс предметов/жидкости/топлива сохраняется; смена/снятие/reload не теряют содержимое; ограничения доступа/дистанции действуют |
| 5. Профессии и AI поведения | Полные циклы каждой профессии, единый путь разрешений изменения мира, инструменты/тара, цели/прерывания; боевые механики | Дефекты 10–11, 16–17; замены honey/crossbow/trident; после 2 и 4 | По сценарию на каждую исходную профессию; работа на пустом участке; отменённое действие ничего не расходует; корректные stop/resume и reload |
| 6. Блоки, сущности и игры | Полный lifecycle мебели/структур/мест, beacon/model switcher/scarecrow, транспорт/спавн, правила игр | Дефекты 03, 26, 29; после 1–2 | Размещение→использование→сохранение→разрушение воспроизводит SRC; занятость мест и результаты партий корректны; нет двойного дропа |
| 7. Клиент, модели и звук | Все необходимые экраны/настройки, анимации и pack selection; sound pack, Mute и TTS | Дефекты 21–22; частичный движок анимации; после серверных контрактов | Клиент отображает реальное серверное состояние; проверены модели с различными контроллерами; выбор звука и Mute слышимы в двух клиентах |
| 8. AI чат и фоновые службы | Сопоставить providers/tools/config/history SRC, реализовать отсутствующие возможности, лимиты очередей и жизненный цикл | Дефект 23; текущий упрощённый чат | Таблица возможностей без скрытых заглушек; медленный/ошибающийся сервис не накапливает работу без границ; выход из мира очищает работу |
| 9. API, интеграции и контент | Контракты расширений, реальные compat handlers, loot/achievements, модовые растения/животные/инструменты, локализация | Дефект 25; зависит от контрактов 4–8 | Каждый заявленный API/compat подтверждён вызывающим кодом и тестом; рецепты/loot исполняются в 1.7; miner проверен отдельно как добавка |
| 10. Приёмка сборки | Полная матрица ниже, миграция, два клиента, dedicated, длительная работа и ресурсы распространения | После 0–9 | Записаны результаты и оставшиеся ограничения; нет открытых блокирующих потерь/дюпов; состав артефактов и разрешения на ресурсы проверены |

### Обязательное раскрытие объёма этапов

- **Профессии:** idle, attack, ranged, danmaku, crossbow, trident, farm, sugar cane, melon/pumpkin, cocoa, grass, snow, feed owner, feed animal, shear, milk, torch, fishing, extinguishing, honey, board games. Miner — отдельная добавка LEGACY. Для каждой сравнить поиск цели, навигацию, условия работы, инвентари, расход/остатки, отмену и результат.
- **Состояние горничной:** tame/owner, лимиты владельца, сидение, бой при низком здоровье, DAY/NIGHT/ALL, home, кровать, еда, pickup, favorability, смерть/воскрешение, фото/slab и backups. Проверять также выгрузку чанка и смену измерения.
- **Блоки и сущности:** altar, bed, shrine, picnic, snack cabinet, statue/garage kit/chisel, beacon, model switcher, scarecrow, keyboard/bookshelf/computer, три настольные игры, broom/chair/box/PowerPoint/fairy. Для каждого — ориентация, структура, NBT, drop, доступ и tracking.
- **Специфика версий:** отсутствие в vanilla 1.7.10 пчёл, современных снарядов, data components/современных NBT и animation runtime требует явного решения по каждой механике. Подмена стрелой или производством продукта по таймеру не подтверждает эквивалентность.
- **Интеграции:** перечислить каждый реально поддерживаемый мод и функцию. Обнаружение установленного мода или наличие OreDictionary не подтверждает поддержку его инструментов и API.

## Приёмочные проверки

| Проверка | Минимальный сценарий | Необходимое свидетельство |
|---|---|---|
| Сборка и запуск | Java 8; принудительная компиляция; production/dev; клиент и dedicated | Команды, версия, журнал успешного запуска без отсутствующих классов; test NO-SOURCE не считать тестами |
| Выживание | Новый мир, gohei→алтарь→горничная→специальные рюкзаки | Достижимая цепочка рецептов без `/give`, корректные расходы и остатки |
| Сохранность | Полные стаки, заполненные инвентари, неудачное размещение, смена рюкзака, смерть/restore | Количество и полный NBT до/после; нет лишних предметов и пропавших данных |
| Миграция | Реальные SRC NBT: руки/броня, зачарования/прочность, все рюкзаки, home/pickup, задачи/игры/history | Ожидаемые значения после импорта и повторного сохранения; явный результат для неизвестных ID |
| Мультиплеер | Dedicated + владелец и второй игрок; чужие/дальние/некорректные запросы | Одинаковые руки/слоты/задачи после tracking/reload; запрещённые действия отклонены без изменения состояния |
| Профессии | Каждый режим: штатный цикл, нет ресурсов, полный выход, sitting, расписание, reload | Наблюдаемые результаты SRC и LEGACY, расход/тара/прочность; отмена действия защитой мира |
| Мир и мебель | Все структуры/сиденья, занятой объект, ломание каждой части, redstone, измерения | Нет осиротевших половин/пассажиров, двойного дропа и утраты NBT |
| Игры | Победа/поражение/ничья, копирование состояния, reload, несколько одновременных партий | Верные результаты/награды и измеренное время серверного тика |
| Клиент | Выбор моделей/звуков, анимации, resource reload, GUI, локализации, Mute/TTS | Скриншоты/наблюдения по выбранным сценариям и список неподдерживаемых контроллеров |
| Нагрузка | 20 активных горничных на 10 минут; медленный AI и backup; выход/вход в мир | Характеристики машины, средний tick и выбросы, память/размеры очередей; целевой средний tick <50 мс, отсутствие неограниченного роста |

## Имеющиеся доказательства и ограничения

На 2026-09-15 выполнены `gradlew.bat --offline build` и `gradlew.bat --offline compileJava --rerun-tasks`: успешно. Автотесты отсутствуют (`test NO-SOURCE`). Игровой клиент, dedicated server и postInit self-test этим аудитом не запускались. Ранее записанные в старой карте игровые успехи не перенесены в статусы «Принято» без повторной проверки конкретных сценариев.

В production JAR найдены 13 классов игровых движков, в dev JAR — 0. Проверенные ссылки model/texture и прямые OGG разрешаются, дубли ZIP-путей не найдены; это не визуальная проверка. Инвентаризация: `PORT_AUDIT_INVENTORY.json`; воспроизведение: `audit_inventory.py`. Снимок содержит 1488 Java-файлов SRC и 198 LEGACY; это не мера готовности.

### Комплектация выпуска

До распространения проверить лицензии кода и каждого набора ресурсов и добавить необходимые уведомления. Предыдущая проверка не обнаружила корневых LICENSE/COPYING/NOTICE; это открытый пункт комплектации, а не доказательство отсутствия разрешений.

Сохранённые сведения об авторах из manifest: основной pack — Succinum, Pajinyi, Hoishi, ZeniCrow, Paulzzh, Tian_mi, CrystalizedSun, FumoLover; old pack — Hoishi, Succinum, Pajinyi, ZeniCrow, FumoLover; Seihou — TartaricAcid и CrystalizedSun; Minecraft 15th — CrystalizedSun; Gecko/credits — авторы отдельных manifest. Peco — CV из `littlemaid_peco/maid_sound.json` и Tamemaru, ссылка manifest: https://booth.pm/ja/items/1903163. Эти сведения не заменяют проверку лицензий.

## Подробная база карты

Ниже сохранены 34 дефекта (14 P1, 20 P2), подтверждённые кодом; визуальная часть №34 также подтверждена снимком пользователя, ссылки на реализации и матрица остальных подсистем. Номера соответствуют этапам выше. Сценарии описаны, но не объявлены выполненными. Изменение документации не исправляет игровой код.

## Подтверждённые дефекты

P1 — блокирует важную функцию либо приводит к потере/дублированию данных. P2 — функциональная ошибка или существенное расхождение поведения.

### 01. [P1] Алтарь нельзя получить обычным путём выживания

SRC создаёт мультиблок через `ItemHakureiGohei.useOn/checkAndBuild`. LEGACY регистрирует отдельный `ALTAR`, но его gohei — пустой подкласс Item с настройками имени/прочности; формирования алтаря нет. Среди регистраций обычных/алтарных рецептов и лута нет выдачи ALTAR. Перенос JSON в JAR этого не исправляет: Forge 1.7.10 не исполняет современные рецепты.

Проверка: новый survival-мир, построить исходную структуру и применить gohei; альтернативного рецепта блока также нет. Нужен исполняемый путь создания алтаря в 1.7.

Код: [legacy/ItemHakureiGohei.java:8](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/item/ItemHakureiGohei.java:8), [legacy/LegacyRecipes.java:12](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/init/LegacyRecipes.java:12), [legacy/ModBlocks.java:65](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/init/ModBlocks.java:65), [main/ItemHakureiGohei.java:71](C:/Users/brawl/Desktop/mods/TLMM/src/main/java/com/github/tartaricacid/touhoulittlemaid/item/ItemHakureiGohei.java:71).

### 02. [P1] GUI алтаря не содержит ни одного слота приношений

`TileEntityAltar` имеет 6 слотов, `BlockAltar` вызывает `displayGUIChest(altar)`. В локальных исходниках Minecraft 1.7.10 `ContainerChest` вычисляет `numRows = size / 9`; для 6 это 0. В GUI создаются только слоты игрока. Наполнить алтарь вручную через этот экран нельзя.

Нужен собственный шестислотовый контейнер и соответствующий клиентский экран. Проверка: открыть даже выданный через creative алтарь.

Код: [legacy/TileEntityAltar.java:16](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/tileentity/TileEntityAltar.java:16), [legacy/BlockAltar.java:26](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/block/BlockAltar.java:26).

### 03. [P1] Неудачная установка кровати возвращает весь исходный стак

Когда для второй половины нет места, `onBlockPlacedBy` добавляет в инвентарь `stack.copy()` без ограничения количества. Стандартный `ItemBlock` 1.7 после callback списывает только одну единицу. При нескольких кроватях и свободных слотах игрок получает лишние предметы. При одном предмете фактический возврат также зависит от вместимости инвентаря.

Проверка: взять стак из нескольких кроватей, заблокировать место второй половины и попытаться поставить. Проверку размещения нужно выполнить до установки/списания, либо вернуть ровно один предмет с обработкой остатка.

Код: [legacy/BlockMaidBed.java:53](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/block/BlockMaidBed.java:53).

### 04. [P1] Крафт алтаря удаляет целиком все стаки ингредиентов

Алтарь допускает стаки до 64; совпадение рецепта проверяется по занятым слотам. После одной выдачи результата вызывается `clear`, обнуляющий все слоты. Например, по 64 ингредиента в каждом слоте превращаются в один результат и полностью исчезают. Сейчас это достижимо через автоматизацию/заполнение NBT; после исправления GUI станет обычным пользовательским сценарием.

Нужно списывать требуемое количество и учитывать контейнеры ингредиентов, либо ограничить слоты одной единицей.

Код: [legacy/BlockAltar.java:54](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/block/BlockAltar.java:54), [legacy/TileEntityInventory.java:46](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/tileentity/TileEntityInventory.java:46), [legacy/LegacyAltarRecipes.java:80](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/crafting/LegacyAltarRecipes.java:80).

### 05. [P1] Печной рюкзак не переносит механику печи

Раз в 200 тиков выбираются первый плавящийся предмет и первое топливо из общего инвентаря; по одной единице каждого списывается на один результат. Нет времени горения, выделенных input/fuel/output, остатка ведра или опыта. Один уголь плавит один предмет вместо использования своего времени горения. Один и тот же стак брёвен может быть выбран одновременно как input и fuel. Состояние печи из `MaidBackpackData.Items/BurnTime/CookTime` не переносится.

Проверка: 8 руды + 1 уголь; отдельно — только брёвна; отдельно — ведро лавы. SRC реализует полноценный автомат печи в `FurnaceBackpackData`.

Код: [legacy/EntityMaid.java:805](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid.java:805), [main/FurnaceBackpackData.java:88](C:/Users/brawl/Desktop/mods/TLMM/src/main/java/com/github/tartaricacid/touhoulittlemaid/entity/backpack/data/FurnaceBackpackData.java:88).

### 06. [P1] Верстак-рюкзак закрывается проверкой vanilla контейнера

Пакет OPEN_BACKPACK вызывает `displayGUIWorkbench` по координатам горничной. `ContainerWorkbench.canInteractWith` в 1.7.10 требует настоящий `Blocks.crafting_table` в этих координатах. Обычно там воздух, поэтому сервер закрывает экран.

Проверка: надеть crafting_table_backpack и открыть вдали от верстака. Нужен контейнер, проверяющий доступ к горничной.

Код: [legacy/MessageMaidConfig.java:74](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/network/message/MessageMaidConfig.java:74), [main/CraftingTableBackpack.java:28](C:/Users/brawl/Desktop/mods/TLMM/src/main/java/com/github/tartaricacid/touhoulittlemaid/entity/backpack/CraftingTableBackpack.java:28).

### 07. [P1] Клиент считает вместимость рюкзака равной 6

Тип синхронизируется DataWatcher, но `getBackpackCapacity()` читает поле `backpackType`, остающееся `empty` у сетевой клиентской сущности. Только `getBackpackType()` читает watcher. Клиентские `Slot.isItemValid/canTakeStack` используют неправильную вместимость и запрещают работу со слотами 6+ даже при большом рюкзаке на сервере.

Проверка: большой рюкзак, обычный клик и перетаскивание в расширенные слоты, повторить после переподключения. Getter вместимости должен использовать синхронизированный тип.

Код: [legacy/EntityMaid.java:728](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid.java:728), [legacy/ContainerMaid.java:32](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/inventory/container/ContainerMaid.java:32).

### 08. [P2] После загрузки клиент видит профессию idle

`readEntityFromNBT` присваивает `taskId` напрямую, не обновляя `WATCHER_TASK_INDEX`. Клиент читает только индекс из watcher, начально равный idle. Сервер может продолжать работать фермером/рыболовом, а GUI, анимации и диагностика показывают idle. Простое повторное назначение того же задания не обязательно исправит это: `switchTask` возвращается при `oldTask == newTask`.

Проверка: сменить профессию, сохранить/перезагрузить мир или чанк и сравнить работу с GUI.

Код: [legacy/EntityMaid.java:625](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid.java:625), [legacy/EntityMaid.java:954](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid.java:954), [legacy/TaskManager.java:101](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/entity/task/TaskManager.java:101).

### 09. [P2] Предметы в руках рендерятся из несинхронизированного хранилища

Vanilla S04 обновляет vanilla equipment, но клиентская ветка `setCurrentItemOrArmor` специально не отражает это в `maidEquipmentInventory`. Рендер читает обе руки именно из `maidEquipmentInventory`. Его содержимое приходит через открытый контейнер, но не через обычный entity tracking; отдельного offhand-пакета нет.

Проверка: второй игрок входит в зону видимости вооружённой горничной, не открывая её GUI; затем владелец меняет оружие. Основную руку нужно читать из корректного зеркала, вторую синхронизировать отдельно.

Код: [legacy/RenderMaid.java:89](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/client/renderer/entity/RenderMaid.java:89), [legacy/EntityMaid.java:719](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid.java:719), [legacy/NetworkHandler.java:19](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/network/NetworkHandler.java:19).

### 10. [P1] Фермер не сажает на пустые грядки

LEGACY ищет исключительно зрелые растения и сбрасывает metadata после сбора. Нет поиска пустой пашни, посадки из семян и режима работы с мотыгой. SRC отдельно выполняет `canPlant/plant` и `MaidFarmPlantTask`, включая пустые посадочные места.

Проверка: пустая увлажнённая пашня + семена у горничной с профессией farm. Нужен перенос посадки, а не только регенерация уже существующего растения.

Код: [legacy/TaskFarm.java:37](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/entity/task/TaskFarm.java:37), [main/TaskNormalFarm.java:100](C:/Users/brawl/Desktop/mods/TLMM/src/main/java/com/github/tartaricacid/touhoulittlemaid/entity/task/TaskNormalFarm.java:100), [main/MaidFarmPlantTask.java:52](C:/Users/brawl/Desktop/mods/TLMM/src/main/java/com/github/tartaricacid/touhoulittlemaid/entity/ai/brain/task/MaidFarmPlantTask.java:52).

### 11. [P1] Сбор урожая обходит разрешения на разрушение

`AbstractHarvestTask` и `TaskFarm` напрямую изменяют блоки/metadata и выдают drops. SRC предварительно вызывает `maid.canDestroyBlock`, включающий проверку блока и Forge event. LEGACY не предоставляет эквивалентного отменяемого пути для этих профессий. Обработчики защиты, рассчитанные на события разрушения, не получают возможности остановить операцию. Наличие BreakEvent у miner не исправляет остальные задачи.

Проверка: запретить горничной сбор через обработчик события; убедиться, что блок и дроп остаются неизменными. Для 1.7 нужен единый адаптер разрешённого действия, используемый всеми задачами изменения мира.

Код: [legacy/AbstractHarvestTask.java:29](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/entity/task/AbstractHarvestTask.java:29), [legacy/TaskFarm.java:67](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/entity/task/TaskFarm.java:67), [main/EntityMaid.java:2406](C:/Users/brawl/Desktop/mods/TLMM/src/main/java/com/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid.java:2406).

### 12. [P1] Миграция предметов из SRC теряет экипировку и свойства

`LegacyNbtMigration` обрабатывает четыре ItemStackHandler-инвентаря, но не `HandItems/ArmorItems`; LEGACY читает собственный `MaidEquipmentInventory`, которого SRC не записывает. Также преобразуется только строковый `id`: нет переноса современной вложенной прочности, зачарований и преобразования flattened vanilla ID в 1.7 item+metadata. Например, `minecraft:oak_planks` не равен реестровому имени 1.7 `minecraft:planks` с metadata 0.

Это не универсальный конвертер сохранений. Если импорт modern NBT поддерживается, нужен явный конвертер каждой структуры и политика для неподдерживаемых предметов, исключающая тихую потерю. Проверять следует на реальном NBT SRC, а не только на вручную созданном apple fixture.

Код: [legacy/LegacyNbtMigration.java:13](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/entity/passive/LegacyNbtMigration.java:13), [legacy/LegacyNbtMigration.java:123](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/entity/passive/LegacyNbtMigration.java:123), [legacy/EntityMaid.java:664](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid.java:664), [main/EntityMaid.java:1361](C:/Users/brawl/Desktop/mods/TLMM/src/main/java/com/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid.java:1361).

### 13. [P2] Названия флагов NBT не совпадают с SRC

SRC сохраняет `MaidIsPickup` и `MaidIsHome`. LEGACY читает/пишет `MaidPickup` и `MaidHomeMode`, а migration их не переименовывает. Импортированная домашняя горничная теряет home mode, а выключенный подбор включается обратно. Перенос координат `MaidSchedulePos` этого не исправляет.

Код: [main/MaidConfigManager.java:12](C:/Users/brawl/Desktop/mods/TLMM/src/main/java/com/github/tartaricacid/touhoulittlemaid/entity/passive/MaidConfigManager.java:12), [legacy/EntityMaid.java:635](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid.java:635).

### 14. [P1] Самостоятельное питание заменено голоданием

В LEGACY есть периодическое уменьшение hunger и урон `starve`, но нет аналога `MaidWorkMealTask`, который использует пищу из рук/рюкзака. Автоматическое питание LEGACY возможно только в IDLE из соседнего TileEntityInventory. На расписании ALL эта ветка недостижима: горничная может голодать с полным рюкзаком еды. В SRC поле hunger по найденным обращениям сохраняется/читается, но соответствующего legacy-циклу истощения и starvation нет.

Проверка: ALL, низкий hunger, еда в рюкзаке, без ручного кормления. Следует восстановить питание SRC и отдельно решить, нужна ли вообще добавленная механика голода.

Код: [legacy/EntityMaid.java:1028](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid.java:1028), [legacy/EntityMaid.java:1040](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid.java:1040), [main/MaidWorkMealTask.java:38](C:/Users/brawl/Desktop/mods/TLMM/src/main/java/com/github/tartaricacid/touhoulittlemaid/entity/ai/brain/task/MaidWorkMealTask.java:38).

### 15. [P2] REST принудительно уводит к неинициализированной точке сна

`tickHomeBehaviors` выставляет sleeping=true по одному расписанию и вызывает `seekBedAndRest` без проверки sitting/home mode. Если домашние точки не настроены, sleep point начально (0,0,0); при отсутствии кровати путь строится туда. Это конфликтует со следованием владельцу, а визуальная поза сна включается даже без кровати. SRC начинает настоящий сон только у найденной незанятой кровати после `canBrainMoving`.

Проверка: свежеприручённая горничная без home mode, ночное время; отдельно дать команду сидеть. Поиск кровати и сон должны учитывать доступность перемещения и реальное место сна.

Код: [legacy/EntityMaid.java:1040](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid.java:1040), [legacy/EntityMaid.java:1051](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid.java:1051), [main/MaidBedTask.java:37](C:/Users/brawl/Desktop/mods/TLMM/src/main/java/com/github/tartaricacid/touhoulittlemaid/entity/ai/brain/task/MaidBedTask.java:37).

### 16. [P2] Кормление владельца не применяет эффекты еды

LEGACY вызывает только `FoodStats.addStats` и удаляет предмет. SRC использует `finishUsingItem`, сохраняя эффекты и возвращаемую тару. Золотое яблоко в порте расходуется без его эффектов; миска супа также не возвращается. При полном hunger и низком здоровье яблоко не используется, хотя в SRC имеет высокий приоритет.

Код: [legacy/TaskFeedOwner.java:27](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/entity/task/TaskFeedOwner.java:27).addStats), [main/TaskFeedOwner.java:102](C:/Users/brawl/Desktop/mods/TLMM/src/main/java/com/github/tartaricacid/touhoulittlemaid/entity/task/TaskFeedOwner.java:102).

### 17. [P2] Некоторые профессии игнорируют отдельные task slots

`TaskFeedOwner.findSafeFood` и `TaskFeedAnimal.findBreedingFood` сканируют только `getMaidInventory`. В GUI при этом есть отдельный девятислотовый task inventory, а общие методы поиска остальных задач умеют искать по логическим слотам. Еда, положенная в task slots, для этих двух профессий невидима.

Проверка: оставить единственную подходящую еду в task inventory. Использовать общий доступный инвентарь с согласованным порядком поиска.

Код: [legacy/TaskFeedOwner.java:59](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/entity/task/TaskFeedOwner.java:59), [legacy/TaskFeedAnimal.java:83](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/entity/task/TaskFeedAnimal.java:83), [legacy/ContainerMaidTask.java:10](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/inventory/container/ContainerMaidTask.java:10).

### 18. [P2] Wireless IO изменил слот установки и потерял ограничение дальности

SRC регистрирует Wireless IO как bauble и ограничивает расстояние до привязанного блока радиусом горничной. LEGACY не допускает ItemWirelessIO в ContainerMaidBauble и сканирует его только в общем рюкзаке. При передаче проверяется измерение и загруженность чанка, но не расстояние. Связь продолжает работать на любом удалении в одном измерении, пока чанк загружен. Настройки отдельных слотов/сторон инвентаря SRC также заменены простым фильтром предметов.

Проверка: установить по исходной схеме в bauble slot; отдельно разместить в рюкзаке и отвести горничную далеко от загруженного сундука.

Код: [legacy/ContainerMaidBauble.java:19](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/inventory/container/ContainerMaidBauble.java:19), [legacy/EntityMaid.java:1144](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid.java:1144), [main/WirelessIOBauble.java:105](C:/Users/brawl/Desktop/mods/TLMM/src/main/java/com/github/tartaricacid/touhoulittlemaid/item/bauble/WirelessIOBauble.java:105).

### 19. [P2] Прочность защитных bauble занижена до 6

LEGACY задаёт всем повреждаемым bauble 6. В SRC explosion/fall имеют 32, fire/magic — 128, projectile/drown/nimble — 64; только elixir имеет 6. Это отдельная ошибка баланса, не обусловленная API 1.7.

Код: [legacy/ModItems.java:43](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/init/ModItems.java:43), [main/InitItems.java:31](C:/Users/brawl/Desktop/mods/TLMM/src/main/java/com/github/tartaricacid/touhoulittlemaid/init/InitItems.java:31).

### 20. [P1] Рецепт scarecrow требует несуществующий survival-ингредиент

Вместо современного granite указан `Items`-вид `Blocks.stone` с metadata 1. В vanilla 1.7.10 гранита нет; обычный stone получается с metadata 0. Следовательно, без стороннего способа выдать нестандартный metadata рецепт невыполним.

Нужно выбрать явно доступный 1.7 заменитель либо OreDictionary-альтернативы. Нельзя переносить metadata гранита из 1.8+ в 1.7.

Код: [legacy/LegacyAltarRecipes.java:45](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/crafting/LegacyAltarRecipes.java:45). SRC: `src/main/resources/data/touhou_little_maid/recipes/altar/craft_scarecrow.json`.

### 21. [P2] Выбор голосового пакета не влияет на голос

`soundPackId` сохраняется и синхронизируется, но `playMaidVoice` всегда воспроизводит `touhou_little_maid:<event>`. В SRC при воспроизведении передаётся `getSoundPackId()` в `PlayMaidSoundMessage`. В LEGACY нет соответствующего выбора звука из выбранного пакета.

Проверка: назначить два разных доступных пакета двум горничным и вызвать одинаковое событие. Синхронизация строки без использования при воспроизведении не является портом функции.

Код: [legacy/EntityMaid.java:945](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid.java:945), [main/EntityMaid.java:1769](C:/Users/brawl/Desktop/mods/TLMM/src/main/java/com/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid.java:1769).

### 22. [P2] TTS обходит Mute

Обычные голоса проверяют `isMuted()`, но отправка MessageMaidTts и DynamicTtsPlayer.play не проверяют mute ни на сервере, ни на клиенте. Горничная с Mute продолжает озвучивать ответы AI.

Код: [legacy/LegacyMaidChatService.java:51](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/ai/LegacyMaidChatService.java:51), [legacy/DynamicTtsPlayer.java:4](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/client/sound/DynamicTtsPlayer.java:4).

### 23. [P2] Очереди фоновой работы не ограничены

`newFixedThreadPool(2)` и `newSingleThreadExecutor` используют неограниченные очереди; server dispatcher также использует `ConcurrentLinkedQueue`. Ограничение числа потоков/сообщений в history не ограничивает число ожидающих HTTP-запросов, NBT-снимков или пакетных действий. При медленном сервисе/диске и поступлении быстрее обработки накопление не ограничено; отсутствует backpressure и очистка жизненного цикла мира.

Нужны ограниченные очереди, запрет нескольких запросов на одну горничную и корректное завершение/сброс. Игровая нагрузка и расход памяти в этом аудите не измерялись.

Код: [legacy/LegacyMaidChatService.java:32](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/ai/LegacyMaidChatService.java:32), [legacy/MaidBackupsManager.java:26](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/world/backups/MaidBackupsManager.java:26), [legacy/ServerThreadDispatcher.java:13](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/network/ServerThreadDispatcher.java:13).

### 24. [P1] dev JAR не содержит используемые классы шахматных движков

Основной `jar` дополнен `sourceSets.engine.output`; `devJar` собирает только `sourceSets.main.output`. Инспекция ZIP подтверждает 13 engine classes в production и 0 в dev. TileEntityCChess/WChess напрямую ссылаются на них, а postInit self-test создаёт игровые объекты. Самостоятельное использование dev-артефакта не имеет нужного runtime-кода.

Нужно одинаково включить engine output в оба артефакта и проверять состав обоих.

Код: `build.gradle`, блоки `jar.from sourceSets.engine.output` и `task devJar`; [legacy/TileEntityWChess.java:10](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/tileentity/TileEntityWChess.java:10).

### 25. [P2] Публичное расширение профессий ломает startup self-test

TaskManager предоставляет `register(IMaidTask)`, но postInit требует `getTasks().size() == 22`. Аддон, корректно зарегистрировавший дополнительную профессию до postInit, приводит к исключению и срыву запуска. Требуется проверять наличие обязательных ID, а не запрещать любое расширение размером коллекции.

Код: [legacy/LegacyPortSelfTest.java:31](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/test/LegacyPortSelfTest.java:31), [legacy/TaskManager.java:64](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/entity/task/TaskManager.java:64).

### 26. [P2] Ничья в шахматах засчитывается как победа

`ended()` объединяет checkmate, repeat и moveLimit. После хода игрока BlockBoardGame вызывает `recordBoardWin` для любого `ended()`. Поэтому повторение позиции/лимит ходов выдаёт победу и favorability. Нужно различать победу, поражение и ничью, а не использовать общий флаг завершения.

Проверка: завершить ход игрока повторением позиции или достижением лимита без мата.

Код: [legacy/TileEntityWChess.java:19](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/tileentity/TileEntityWChess.java:19), [legacy/BlockBoardGame.java:187](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/block/BlockBoardGame.java:187), [legacy/BlockBoardGame.java:201](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/block/BlockBoardGame.java:201).

### 27. [P2] Tank backpack поглощает жидкость, но не выдаёт её обратно

В LEGACY есть только перенос воды/лавы/молока из полных вёдер в поля `backpackFluid/Amount`; интерфейс выводит текст. Нет output-контейнера или обработчика наполнения пустой тары, в ItemMaidBackpack нет fluid API. Снятие рюкзака сохраняет NBT, но не делает жидкость доступной. SRC реализует вход/выход и FluidUtil в tank-контейнере.

Проверка: наполнить рюкзак, затем попытаться получить полное ведро из него. Нужен двусторонний перенос с сохранением объёма.

Код: [legacy/EntityMaid.java:811](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid.java:811), [legacy/ItemMaidBackpack.java:7](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/item/ItemMaidBackpack.java:7), [main/TankBackpackContainer.java:36](C:/Users/brawl/Desktop/mods/TLMM/src/main/java/com/github/tartaricacid/touhoulittlemaid/inventory/container/backpack/TankBackpackContainer.java:36).

### 28. [P2] Film сохраняет опасные состояния смерти

SRC удаляет `Fire`, `Air`, `FallDistance`, `ActiveEffects`, `Leash` и выключает home mode перед записью Film. LEGACY очищает только часть тегов: огонь, эффекты и другие состояния остаются и загружаются при воскрешении. Восстановление health не очищает их. Например, умершая в огне горничная после воскрешения продолжает гореть; старый home mode также сохраняется.

Нужно очистить состояния по семантике SRC с учётом vanilla 1.7 NBT. Проверка: смерть в огне/с негативными эффектами и воскрешение в безопасном месте.

Код: [legacy/ItemFilm.java:25](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/item/ItemFilm.java:25), [main/ItemFilm.java:80](C:/Users/brawl/Desktop/mods/TLMM/src/main/java/com/github/tartaricacid/touhoulittlemaid/item/ItemFilm.java:80).

### 29. [P1] Разрушение изголовья кровати удаляет предмет без дропа

`BlockMaidBed.breakBlock` создаёт предмет только при `!head`. При разрушении изголовья этот путь пропускается; затем вторая половина удаляется под `removingOtherHalf`, и её callback также пропускает выдачу. `getItemDropped` всегда возвращает null. В результате в survival обе половины исчезают без предмета. Обратная ветка тоже некорректна: удаление нижней половины создаёт EntityItem без проверки creative. В SRC creative обрабатывается отдельно, а loot table выдаёт предмет через половину head.

Нужен единый lifecycle двух половин: один предмет с сохранённым цветом при survival-разрушении любой половины и отсутствие предметов при creative-разрушении. Он должен учитывать также неудачное размещение из №03, чтобы откат не выдавал лишний дроп.

Проверка после исправления: поставить цветную кровать; отдельно разрушить каждую половину в survival и creative; проверить удаление обеих частей, количество предметов и BedColor. Вывод подтверждён статически 2026-10-02, игровой сценарий не запускался.

Код: [legacy/BlockMaidBed.java:92](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/block/BlockMaidBed.java:92), [legacy/BlockMaidBed.java:105](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/block/BlockMaidBed.java:105), [legacy/BlockMaidBed.java:119](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/block/BlockMaidBed.java:119), [main/BlockMaidBed.java:111](C:/Users/brawl/Desktop/mods/TLMM/src/main/java/com/github/tartaricacid/touhoulittlemaid/block/BlockMaidBed.java:111), [SRC loot:12](C:/Users/brawl/Desktop/mods/TLMM/src/main/resources/data/touhou_little_maid/loot_tables/blocks/maid_bed.json:12).

### 30. [P2] Надевание аксессуара через ПКМ обходит закрытые слоты UI

Контейнер разрешает 10/20/30 слотов по уровню благосклонности, но `EntityMaid.interact` ищет свободное место во всех 30 слотах. `findBauble` также активирует аксессуары из всех 30. При начальном уровне и заполненных первых десяти слотах следующий аксессуар уходит в невидимый/недоступный через GUI слот и действует оттуда. Дополнительно действуют аксессуары из обычного рюкзака, что уже отмечено в матрице.

Проверка: низкая благосклонность, заполнить десять доступных слотов, применить одиннадцатый аксессуар через ПКМ. Требование: один лимит доступных слотов для GUI, прямого надевания и исполнения эффектов; скрытые предметы не должны становиться недоступными без определённой политики возврата.

Код: [EntityMaid.java:301](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid.java:301), [EntityMaid.java:1077](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid.java:1077), [ContainerMaidBauble.java:17](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/inventory/container/ContainerMaidBauble.java:17).

### 31. [P2] Полоса благосклонности использует неверный знаменатель

UI делит суммарные очки на `nextLevelPoint()`, который возвращает оставшиеся очки до следующего уровня. При 32/64 индикатор уже заполнен на 100%; при 64 очках он падает до 50%, хотя новый уровень только начался. SRC вычисляет долю внутри текущего уровня в `getLevelPercent()`; на максимальном уровне его результат также отличается от LEGACY.

Проверка: значения 0, 32, 63, 64, 128, 191, 192, 383, 384. Сверять отдельно число очков, уровень и заполнение полосы.

Код: [AbstractGuiMaid.java:126](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/client/gui/AbstractGuiMaid.java:126), [legacy/FavorabilityManager.java:25](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/entity/favorability/FavorabilityManager.java:25), [SRC/FavorabilityManager.java:128](C:/Users/brawl/Desktop/mods/TLMM/src/main/java/com/github/tartaricacid/touhoulittlemaid/entity/favorability/FavorabilityManager.java:128).

### 32. [P2] Подпись кнопки расписания остаётся предыдущей после переключения

`actionPerformed` отправляет пакет и сразу читает `shortSchedule()` из ещё не обновлённого DataWatcher. Получив серверное состояние позднее, GUI не обновляет сохранённый `displayString`: нет updateScreen, который перечитывал бы расписание. Подпись может оставаться старой до пересоздания экрана, а при следующих нажатиях отставать на шаг. Аналогично доступность кнопки рюкзака вычисляется лишь в initGui.

Проверка: переключить DAY→NIGHT на открытом экране и дождаться ответа сервера; сравнить строку и фактическое расписание. Требование: обновлять виджеты из принятого серверного состояния, без зависимости от нового клика.

Код: [GuiMaid.java:57](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/client/gui/GuiMaid.java:57), [MessageMaidConfig.java:52](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/network/message/MessageMaidConfig.java:52).

### 33. [P2] Маска слотов аксессуаров меняется независимо от контейнера

`ContainerMaidBauble` фиксирует количество слотов в конструкторе. `GuiMaidBauble` каждый кадр читает текущий уровень благосклонности. Если уровень меняется при открытой вкладке (например, срабатывает автоматическое событие еды рядом с домашним контейнером), маска показывает уже другой набор доступных ячеек, чем существует в контейнере. SRC экран сохраняет уровень при создании, согласуя маску со сформированным контейнером.

Проверка: открыть вкладку на границе 191/192 или 383/384, изменить уровень при открытом GUI; проверить клики и shift-click. Нужно либо синхронно пересоздать контейнер/экран, либо сохранять одинаковый снимок состояния до закрытия.

Код: [ContainerMaidBauble.java:16](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/inventory/container/ContainerMaidBauble.java:16), [GuiMaidBauble.java:31](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/client/gui/GuiMaidBauble.java:31), [SRC/BaubleContainerScreen.java:38](C:/Users/brawl/Desktop/mods/TLMM/src/main/java/com/github/tartaricacid/touhoulittlemaid/client/gui/entity/maid/backpack/BaubleContainerScreen.java:38).

### 34. [P2] Компоновка статусов перекрывает текст, часть MaidUI не локализована

Предоставленный снимок показывает плохо читаемые DEF/EXP/FAV и имя под вкладками. В коде подписи на y=114/125/136/147 помещены непосредственно на полосы y=115/126/137/148; имя x=84,y=16 пересекается с областью вкладок. SRC отдельно рисует шкалу, иконку и компактное число справа, без этих наложений.

`Inventory`, `Equipment`, `Main`, `Off`, `Task configuration`, `Hidden item`, `Profession tools` жёстко записаны в Java. H/P/S/B не имеют поясняющих hover-подсказок. На снимке длинное название профессии обрезается; простое ограничение ширины предотвращает выход за кнопку, но не заменяет подсказку с полным названием. Исправления локализации предметов из предыдущего прохода эти строки UI не затрагивали.

Проверка: русский/английский язык, разные масштабы GUI, длинное имя горничной/профессии, все четыре вкладки. Требование: разделить зоны текста/полос/вкладок, перевести подписи и добавить объяснения кнопок и усечённых названий. Снимок не позволяет точно установить причину глубинного перекрытия OpenGL, поэтому состояние depth/lighting следует проверить в игре отдельно.

Код: [AbstractGuiMaid.java:123](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/client/gui/AbstractGuiMaid.java:123), [AbstractGuiMaid.java:140](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/client/gui/AbstractGuiMaid.java:140), [GuiMaid.java:31](C:/Users/brawl/Desktop/mods/TLMM/src/legacy/java/com/github/tartaricacid/touhoulittlemaid/client/gui/GuiMaid.java:31), [SRC/AbstractMaidContainerGui.java:609](C:/Users/brawl/Desktop/mods/TLMM/src/main/java/com/github/tartaricacid/touhoulittlemaid/client/gui/entity/maid/AbstractMaidContainerGui.java:609).

## Матрица остальных подсистем и полноты переноса

Эта таблица фиксирует результат чтения реализаций и предел проверки, а не заменяет игровые тесты галочками.

| Подсистема SRC | Что есть в LEGACY | Оценка / что остаётся |
|---|---|---|
| Lifecycle, proxies, Forge registrations | Отдельная Java 8 ветка, FML события, 13 mod entity registrations | Компиляция подтверждена; dedicated/client startup текущего дерева не подтверждён этим аудитом |
| Модель горничной, tame/owner, базовые attributes | EntityTameable, cake, owner check, HP/damage/favorability | Основа есть; питание, NBT, REST и синхронизация имеют дефекты выше |
| Combat: attack, bow, danmaku | Собственные melee/ranged цели, стрельба, enchanted arrows/danmaku | Реальные реализации; баланс, projectile friendly-fire и поведение на сложной местности требуют игрового сравнения |
| Crossbow / trident | Собственные предметы 1.7, фактически EntityArrow | Замена: зарядка арбалета, loyalty/riptide/channeling трезубца не перенесены |
| idle | Игра в снежки, простые idle-ветки | Частичная реализация, не весь brain SRC |
| farm, cocoa | Сбор и сброс возраста существующих культур | Посадка на новые места не перенесена; farm №10 |
| sugar_cane, melon, grass, snow | Собственные harvest-классы, vanilla блоки | Требуются единые разрешения, сверка Fortune/дропа/прочности; специальная логика модовых культур SRC не перенесена |
| feed, feed_animal | Прямое кормление/размножение/выбраковка | №16–17; фильтры и настройки SRC урезаны |
| shears, milk | Vanilla овцы/коровы, расход инструментов/вёдер | Не эквивалент общим расширяемым обработчикам/модовым животным SRC |
| torch, extinguishing | Поиск темноты/огня, постановка факела, extinguishing entity | Реализации есть; отмена действий и взаимодействие с защитой требуют принятия |
| fishing | Собственный bobber, FishingHooks, прочность удочки | Проверить физику поплавка, длительную рыбалку, смену расписания/выгрузку riding-сущностей |
| honey | Производство продукта рядом с цветком каждые 600 ticks | Подмена механики: не сбор зрелого улья, нет обязательных бутылок/ножниц и поведения пчёл |
| miner | EXTRAS с OreDictionary, ограниченным сканером, BreakEvent/HarvestDrops | В SRC базовой профессии miner нет; это отдельная добавка. Отдельно нужны испытания реальных GT/IC2/GC/TConstruct инструментов |
| Расписание | DAY/NIGHT/ALL, пороги 0/8000/12000/16000 | Сами временные пороги совпадают с InitEntities SRC; действия сна не совпадают |
| Home, navigation, follow | SchedulePos, vanilla FollowOwner, safeTeleportNear | Home teleport-back SRC отсутствует в SchedulePos; обычная ветка follow всё ещё использует vanilla fallback; межмировой follow требует отдельного испытания |
| Backpacks | Размеры 6/12/24/36/18 совпадают с BackpackLevel SRC | Специальные рюкзаки и клиентские слоты не приняты: №05–07,27 |
| Baubles/favorability | Пороги 64/192/384 и HP/attack совпадают | Прочности ошибочны; эффекты дополнительно срабатывают из обычного рюкзака; весь набор событий/бонусов SRC не перенесён |
| Смерть, tombstone, photo/slab, backups | Сохранение/восстановление, loaded-UUID guard | Guard проверяет только загруженных сущностей, не всех сохранённых в мире. Unload→restore требует отдельного anti-dup теста; №12,28 |
| Altar, recipes, progression | 42 записи + отдельная ветка rebirth | Количество рецептов не доказательство; №01–04,20. Теги ингредиентов SRC заменены конкретными vanilla items, новые обычные рецепты меняют прогрессию |
| Shrine / picnic / snack cabinet | Собственные inventories, прямые взаимодействия/еда | Упрощены; shrink/остатки, многопользовательский доступ, размещение/удаление seats требуют проверки |
| Maid bed | Две половины, цвет, TESR | Дюп №03, потеря дропа №29; занятость кровати в поиске сна не учитывается как в SRC |
| Statue / Garage Kit / Chisel | NBT фото, структура статуи, обжиг, TESR | Код имеется; цикл создать→обжечь→сломать→поставить с NBT не запускался |
| Beacon / Model Switcher / Scarecrow | Состояние, UI/packets, S35, spawn exclusion | Есть исполняемые реализации. Эффекты пересекающихся beacon, смена владельца привязанной maid и redstone после reload требуют отдельной проверки |
| Board games | Gomoku эвристика, исходные chess/xiangqi engines, board states, seats/proxy blocks | Не одно лишь наличие классов; ошибки №24,26. Поиск chess вызывается синхронно на сервере; нагрузка и правила сохранения партий не приняты |
| Broom, Chair, Box, PowerPoint, Fairy | Собственные сущности и NBT | Проверить tracking, пассажиров, полёт в multiplayer и спавн. Single-rider broom — адаптация, не полное исходное поведение |
| GUI / сеть | 6 container-классов, 11 packet registrations, thread dispatch, ряд owner/distance/bounds checks | Расширенные экраны настройки SRC отсутствуют; №07–09,18,23. Не все проверки runtime/конкурентного доступа подтверждены |
| Bedrock / Gecko / Molang | JSON geometry, quad renderer, фиксированные анимации известных костей | Геометрия есть; произвольные JS/Molang/Gecko controllers не исполняются. Это частичная совместимость моделей, не полноценный движок анимации SRC |
| Ресурсы / локализация | Ресурсы SRC, перенос texture paths, .lang | Проверенные ссылки ресурсов исправны; визуальная правильность всех моделей и полнота всех переводов этим не доказаны |
| Звук / AI / TTS | HTTP chat, история, три вида text actions, WAV | Система 141 AI-класса SRC заменена 2 классами; нет эквивалента полного agent/tool/provider/config UI. №21–23 |
| Loot / advancements | PowerPoint в 6 ChestGenHooks, несколько achievements | Большинство loot tables/модификаторов SRC и критериев advancement не перенесено; generated JSON не становится рабочим автоматически |
| API / интеграции | Один task API, OreDictionary, honey/miner adapters, log detection | Нет аналога большей части 85 API и 152 compat классов SRC. Лог «mod detected» не равен реализации интеграции; расширение task дополнительно сломано №25 |

## Проверки, необходимые после исправлений

1. Изолированный новый survival-мир: получить алтарь и пройти всю цепочку рецептов до горничной/рюкзаков, без `/give`.
2. Inventory conservation: успешная/неудачная установка кровати, крафт стаками, снятие рюкзака, печь/бак, смерть/воскрешение, photo/slab. Сравнить количество и полный NBT до/после.
3. Dedicated server + два клиента: расширенные слоты, обе руки, профессия после reload, model/sound selection, Mute/TTS, доступ чужого игрока.
4. Реальные снимки SRC: руки, броня, damaged/enchanted вещи, все backpack data, home/pickup, task/game/history. Неподдерживаемые предметы должны сохраняться явно, а не исчезать молча.
5. По сценарию на каждую из 21 исходных профессий; отдельно EXTRAS miner. Проверить инструменты/тару, пустую грядку, защиту блоков, sitting, ALL/DAY/NIGHT, unload/reload.
6. Все блоки/сущности: placement, NBT round-trip, break/drop, перенос измерения и resource reload. Включить занятые seats/кровати и существующую maid при restore.
7. Chess/gomoku: победа, поражение, ничья, копия board state, продолжение после reload; измерить серверный tick при нескольких партиях.
8. Нагрузить очередь AI медленным/ошибающимся локальным stub-сервисом и backup очередь медленной записью. Проверить лимиты и очистку при выходе из мира.

До этих проверок корректный статус: **собираемый частичный порт с подтверждёнными дефектами**, а не «портирование завершено».
