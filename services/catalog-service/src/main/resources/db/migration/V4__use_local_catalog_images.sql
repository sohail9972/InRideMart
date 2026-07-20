UPDATE catalog_products SET image_url = '/images/catalog/journey-essentials.svg'
WHERE sku IN ('IRM-NECK-001', 'IRM-UMBRELLA-001');

UPDATE catalog_products SET image_url = '/images/catalog/snacks.svg'
WHERE sku IN ('IRM-SNACK-001', 'IRM-CHIPS-001', 'IRM-PROTEIN-001', 'IRM-CHOC-001', 'IRM-COOKIE-001', 'IRM-MINT-001', 'IRM-DRYFRUIT-001');

UPDATE catalog_products SET image_url = '/images/catalog/drinks.svg'
WHERE sku IN ('IRM-COFFEE-001', 'IRM-SODA-001', 'IRM-ENERGY-001', 'IRM-WATER-001', 'IRM-JUICE-001', 'IRM-ICOFFEE-001', 'IRM-TEA-001');

UPDATE catalog_products SET image_url = '/images/catalog/tech.svg'
WHERE sku IN ('IRM-CHARGER-001', 'IRM-CABLE-001', 'IRM-PHONECHARGE-001', 'IRM-FASTCABLE-001');

UPDATE catalog_products SET image_url = '/images/catalog/wellness.svg'
WHERE sku IN ('IRM-BALM-001', 'IRM-MIST-001', 'IRM-NECKPILLOW-002', 'IRM-EYEMASK-001', 'IRM-PAINSPRAY-001');

UPDATE catalog_products SET image_url = '/images/catalog/meals.svg'
WHERE sku IN ('IRM-NOODLE-001', 'IRM-SANDWICH-001', 'IRM-CUPNOODLE-001');

UPDATE catalog_products SET image_url = '/images/catalog/power.svg'
WHERE sku = 'IRM-POWERBANK-001';

UPDATE catalog_products SET image_url = '/images/catalog/audio.svg'
WHERE sku = 'IRM-EARPHONE-001';

UPDATE catalog_products SET image_url = '/images/catalog/hygiene.svg'
WHERE sku IN ('IRM-WIPES-001', 'IRM-TISSUE-001', 'IRM-SANITIZER-001');

UPDATE catalog_products SET image_url = '/images/catalog/comfort.svg'
WHERE sku = 'IRM-MOTION-001';

UPDATE catalog_products SET image_url = '/images/catalog/rain.svg'
WHERE sku IN ('IRM-UMBRELLA-002', 'IRM-PONCHO-001');
