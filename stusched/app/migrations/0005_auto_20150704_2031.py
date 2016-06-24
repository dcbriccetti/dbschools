# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations


class Migration(migrations.Migration):

    dependencies = [
        ('app', '0004_section_price'),
    ]

    operations = [
        migrations.AddField(
            model_name='section',
            name='max_students',
            field=models.DecimalField(default=6, decimal_places=0, max_digits=3),
        ),
        migrations.AddField(
            model_name='section',
            name='min_students',
            field=models.DecimalField(default=3, decimal_places=0, max_digits=3),
        ),
    ]
