// 碰撞检测系统

export function checkCollision(rect1, rect2) {
    return rect1.x < rect2.x + rect2.width &&
           rect1.x + rect1.width > rect2.x &&
           rect1.y < rect2.y + rect2.height &&
           rect1.y + rect1.height > rect2.y;
}

// 检测子弹和敌人的碰撞
export function checkBulletEnemyCollisions(bullets, enemies, particleSystem) {
    bullets.forEach(bullet => {
        if (bullet.markedForDeletion || bullet.isEnemy) return;

        const bulletBounds = bullet.getBounds();

        enemies.forEach(enemy => {
            if (enemy.markedForDeletion) return;

            const enemyBounds = enemy.getBounds();

            if (checkCollision(bulletBounds, enemyBounds)) {
                // 子弹击中敌人
                bullet.markedForDeletion = true;
                const score = enemy.takeDamage(10); // 每发子弹造成10点伤害

                // 创建火花效果
                particleSystem.createExplosion(bullet.x, bullet.y, bullet.color, 5);

                if (score > 0) {
                    // 敌人死亡
                    particleSystem.createExplosion(enemy.x, enemy.y, enemy.color, 30);
                    return score;
                }
            }
        });
    });
    return 0;
}

// 检测敌人子弹和玩家的碰撞
export function checkEnemyBulletPlayerCollisions(bullets, player, particleSystem) {
    if (player.markedForDeletion) return 0;

    const playerBounds = player.getBounds();
    let totalDamage = 0;

    bullets.forEach(bullet => {
        if (bullet.markedForDeletion || !bullet.isEnemy) return;

        const bulletBounds = bullet.getBounds();

        if (checkCollision(bulletBounds, playerBounds)) {
            bullet.markedForDeletion = true;
            totalDamage += player.takeDamage(10);
            particleSystem.createExplosion(bullet.x, bullet.y, '#f00', 10);
        }
    });

    return totalDamage;
}

// 检测玩家和敌人的碰撞
export function checkPlayerEnemyCollisions(player, enemies, particleSystem) {
    if (player.markedForDeletion) return 0;

    const playerBounds = player.getBounds();
    let totalDamage = 0;

    enemies.forEach(enemy => {
        if (enemy.markedForDeletion) return;

        const enemyBounds = enemy.getBounds();

        if (checkCollision(playerBounds, enemyBounds)) {
            const damage = enemy.type === 'BOSS' ? 30 : 20;
            totalDamage += player.takeDamage(damage);
            enemy.markedForDeletion = true;
            particleSystem.createExplosion(enemy.x, enemy.y, enemy.color, 30);
        }
    });

    return totalDamage;
}

// 综合碰撞检测
export function updateCollisions(bullets, enemies, player, particleSystem) {
    let score = 0;
    let damage = 0;

    // 子弹击中敌人
    enemies.forEach(enemy => {
        bullets.forEach(bullet => {
            if (bullet.markedForDeletion || bullet.isEnemy) return;
            if (enemy.markedForDeletion) return;

            if (checkCollision(bullet.getBounds(), enemy.getBounds())) {
                bullet.markedForDeletion = true;
                const gainedScore = enemy.takeDamage(10);
                particleSystem.createExplosion(bullet.x, bullet.y, bullet.color, 5);

                if (gainedScore > 0) {
                    score += gainedScore;
                    particleSystem.createExplosion(enemy.x, enemy.y, enemy.color, 30);
                }
            }
        });
    });

    // 敌人子弹击中玩家
    if (!player.markedForDeletion) {
        bullets.forEach(bullet => {
            if (bullet.markedForDeletion || !bullet.isEnemy) return;

            if (checkCollision(bullet.getBounds(), player.getBounds())) {
                bullet.markedForDeletion = true;
                damage += player.takeDamage(10);
                particleSystem.createExplosion(bullet.x, bullet.y, '#f00', 10);
            }
        });
    }

    // 玩家撞击敌人
    if (!player.markedForDeletion) {
        enemies.forEach(enemy => {
            if (enemy.markedForDeletion) return;

            if (checkCollision(player.getBounds(), enemy.getBounds())) {
                const collisionDamage = enemy.type === 'BOSS' ? 30 : 20;
                damage += player.takeDamage(collisionDamage);
                enemy.markedForDeletion = true;
                particleSystem.createExplosion(enemy.x, enemy.y, enemy.color, 30);
            }
        });
    }

    return { score, damage };
}
