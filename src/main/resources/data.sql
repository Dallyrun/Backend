INSERT INTO badge (name, description, icon_url, condition_type, condition_value)
VALUES ('첫 러닝', '첫 번째 러닝을 완료했어요!', '/badges/first-run.png', 'TOTAL_COUNT', 1)
ON CONFLICT (name) DO NOTHING;

INSERT INTO badge (name, description, icon_url, condition_type, condition_value)
VALUES ('10회 달성', '10번의 러닝을 완료했어요!', '/badges/10-runs.png', 'TOTAL_COUNT', 10)
ON CONFLICT (name) DO NOTHING;

INSERT INTO badge (name, description, icon_url, condition_type, condition_value)
VALUES ('50회 달성', '50번의 러닝을 완료했어요!', '/badges/50-runs.png', 'TOTAL_COUNT', 50)
ON CONFLICT (name) DO NOTHING;

INSERT INTO badge (name, description, icon_url, condition_type, condition_value)
VALUES ('100회 달성', '100번의 러닝을 완료했어요!', '/badges/100-runs.png', 'TOTAL_COUNT', 100)
ON CONFLICT (name) DO NOTHING;

INSERT INTO badge (name, description, icon_url, condition_type, condition_value)
VALUES ('5K 러너', '한 번에 5km를 달렸어요!', '/badges/5k.png', 'SINGLE_DISTANCE', 5000)
ON CONFLICT (name) DO NOTHING;

INSERT INTO badge (name, description, icon_url, condition_type, condition_value)
VALUES ('10K 러너', '한 번에 10km를 달렸어요!', '/badges/10k.png', 'SINGLE_DISTANCE', 10000)
ON CONFLICT (name) DO NOTHING;

INSERT INTO badge (name, description, icon_url, condition_type, condition_value)
VALUES ('하프 마라톤', '한 번에 21.1km를 달렸어요!', '/badges/half-marathon.png', 'SINGLE_DISTANCE', 21097)
ON CONFLICT (name) DO NOTHING;

INSERT INTO badge (name, description, icon_url, condition_type, condition_value)
VALUES ('풀 마라톤', '한 번에 42.195km를 달렸어요!', '/badges/marathon.png', 'SINGLE_DISTANCE', 42195)
ON CONFLICT (name) DO NOTHING;

INSERT INTO badge (name, description, icon_url, condition_type, condition_value)
VALUES ('100km 돌파', '누적 100km를 달렸어요!', '/badges/100km-total.png', 'TOTAL_DISTANCE', 100000)
ON CONFLICT (name) DO NOTHING;

INSERT INTO badge (name, description, icon_url, condition_type, condition_value)
VALUES ('500km 돌파', '누적 500km를 달렸어요!', '/badges/500km-total.png', 'TOTAL_DISTANCE', 500000)
ON CONFLICT (name) DO NOTHING;

INSERT INTO badge (name, description, icon_url, condition_type, condition_value)
VALUES ('7일 연속', '7일 연속으로 달렸어요!', '/badges/7-day-streak.png', 'STREAK_DAYS', 7)
ON CONFLICT (name) DO NOTHING;

INSERT INTO badge (name, description, icon_url, condition_type, condition_value)
VALUES ('30일 연속', '30일 연속으로 달렸어요!', '/badges/30-day-streak.png', 'STREAK_DAYS', 30)
ON CONFLICT (name) DO NOTHING;

-- 거리 마일스톤 시리즈 (누적)
INSERT INTO badge (name, description, icon_url, condition_type, condition_value)
VALUES ('10K 클럽', '누적 10km를 달렸어요!', '/badges/10k-club.png', 'TOTAL_DISTANCE', 10000)
ON CONFLICT (name) DO NOTHING;

INSERT INTO badge (name, description, icon_url, condition_type, condition_value)
VALUES ('50K 클럽', '누적 50km를 달렸어요!', '/badges/50k-club.png', 'TOTAL_DISTANCE', 50000)
ON CONFLICT (name) DO NOTHING;

INSERT INTO badge (name, description, icon_url, condition_type, condition_value)
VALUES ('100K 클럽', '누적 100km를 달렸어요!', '/badges/100k-club.png', 'TOTAL_DISTANCE', 100000)
ON CONFLICT (name) DO NOTHING;

-- 연속 러닝 시리즈
INSERT INTO badge (name, description, icon_url, condition_type, condition_value)
VALUES ('7일 러너', '7일 연속 러닝!', '/badges/streak-7d.png', 'STREAK_DAYS', 7)
ON CONFLICT (name) DO NOTHING;

INSERT INTO badge (name, description, icon_url, condition_type, condition_value)
VALUES ('30일 러너', '30일 연속 러닝!', '/badges/streak-30d.png', 'STREAK_DAYS', 30)
ON CONFLICT (name) DO NOTHING;

INSERT INTO badge (name, description, icon_url, condition_type, condition_value)
VALUES ('100일 러너', '100일 연속 러닝!', '/badges/streak-100d.png', 'STREAK_DAYS', 100)
ON CONFLICT (name) DO NOTHING;

-- 시간대별 성취
INSERT INTO badge (name, description, icon_url, condition_type, condition_value)
VALUES ('새벽 러너', '새벽(04~06시)에 첫 러닝!', '/badges/early-bird.png', 'EARLY_MORNING_COUNT', 1)
ON CONFLICT (name) DO NOTHING;

INSERT INTO badge (name, description, icon_url, condition_type, condition_value)
VALUES ('심야 러너', '심야(22~03시)에 첫 러닝!', '/badges/night-owl.png', 'LATE_NIGHT_COUNT', 1)
ON CONFLICT (name) DO NOTHING;
