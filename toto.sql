-- 1. Vérification des données disponibles
SELECT
    COUNT(*) as total_orders,
    COUNT(CASE WHEN date >= '2025-06-01' THEN 1 END) as orders_since_june,
    MIN(date) as oldest_date,
    MAX(date) as newest_date
FROM `order`;

-- 2. Vérification des commandes avec cartes
SELECT COUNT(*) as orders_with_cards
FROM `order` o
WHERE EXISTS (
    SELECT 1 FROM card_certification_order cco WHERE cco.order_id = o.id
);

-- 3. Génération de plannings (version simplifiée)
INSERT INTO j_planning (
    id, order_id, employee_id, planning_date, start_time,
    estimated_duration_minutes, priority, status, completed,
    progress_percentage, card_count, created_at, updated_at
)
SELECT
    UNHEX(REPLACE(UUID(), '-', '')),
    o.id,
    emp.employee_id,
    CASE
        WHEN planning_order <= 7 THEN CURRENT_DATE()
        WHEN planning_order <= 14 THEN DATE_ADD(CURRENT_DATE(), INTERVAL 1 DAY)
        WHEN planning_order <= 21 THEN DATE_ADD(CURRENT_DATE(), INTERVAL 2 DAY)
        ELSE DATE_ADD(CURRENT_DATE(), INTERVAL 3 DAY)
        END,
    ADDTIME(
                    CURRENT_DATE(),
                    SEC_TO_TIME(8*3600 + ((planning_order - 1) % 7) * 7200) -- 8h, puis décale de 2h
    ),
    card_count * 3, -- 3 minutes par carte
    CASE
        WHEN card_count >= 15 THEN 'HIGH'
        WHEN card_count >= 5 THEN 'MEDIUM'
        ELSE 'LOW'
        END,
    'SCHEDULED',
    FALSE,
    0,
    card_count,
    NOW(),
    NOW()
FROM (
         -- Commandes avec leur nombre de cartes
         SELECT
             o.id,
             o.num_commande,
             COUNT(cco.card_certification_id) as card_count,
             ROW_NUMBER() OVER (ORDER BY COUNT(cco.card_certification_id) DESC) as planning_order
         FROM `order` o
                  INNER JOIN card_certification_order cco ON o.id = cco.order_id
         WHERE o.date >= '2025-06-01'
         GROUP BY o.id, o.num_commande
         HAVING card_count > 0
             LIMIT 30 -- Limitons à 30 commandes pour commencer
     ) o
         CROSS JOIN (
    -- Employés en rotation
    SELECT
        id as employee_id,
        ROW_NUMBER() OVER (ORDER BY id) as emp_order
    FROM j_employee
    WHERE active = 1
) emp
WHERE (o.planning_order - 1) % 7 + 1 = emp.emp_order; -- Répartition cyclique

-- 4. Vérification des résultats
SELECT
    'PLANNINGS CRÉÉS' as status,
    COUNT(*) as total_plannings,
    SUM(card_count) as total_cards,
    ROUND(SUM(estimated_duration_minutes) / 60, 1) as total_hours
FROM j_planning;

-- 5. Répartition par employé
SELECT
    CONCAT(e.first_name, ' ', e.last_name) as employee,
    COUNT(p.id) as plannings_count,
    SUM(p.card_count) as total_cards,
    ROUND(SUM(p.estimated_duration_minutes) / 60, 1) as total_hours
FROM j_planning p
         JOIN j_employee e ON p.employee_id = e.id
GROUP BY e.id, e.first_name, e.last_name
ORDER BY total_cards DESC;