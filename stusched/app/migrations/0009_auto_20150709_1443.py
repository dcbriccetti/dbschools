# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations


class Migration(migrations.Migration):

    dependencies = [
        ('app', '0008_auto_20150708_1443'),
    ]

    operations = [
        migrations.AddField(
            model_name='section',
            name='scheduled_status',
            field=models.IntegerField(default=1),
            preserve_default=False,
        ),
        migrations.AlterField(
            model_name='section',
            name='max_students',
            field=models.IntegerField(default=6),
        ),
        migrations.AlterField(
            model_name='section',
            name='min_students',
            field=models.IntegerField(default=3),
        ),
    ]
