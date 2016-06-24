# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations


class Migration(migrations.Migration):

    dependencies = [
        ('app', '0005_auto_20150704_2031'),
    ]

    operations = [
        migrations.RenameField(
            model_name='section',
            old_name='start_date',
            new_name='start_time',
        ),
        migrations.AddField(
            model_name='section',
            name='duration',
            field=models.DurationField(default='3 hours'),
            preserve_default=False,
        ),
        migrations.AddField(
            model_name='section',
            name='num_days',
            field=models.DecimalField(default=1, decimal_places=0, max_digits=3),
        ),
    ]
